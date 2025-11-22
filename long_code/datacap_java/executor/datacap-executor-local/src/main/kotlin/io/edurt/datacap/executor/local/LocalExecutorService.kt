package io.edurt.datacap.executor.local

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.Lists
import io.edurt.datacap.common.sql.SqlBuilder
import io.edurt.datacap.common.sql.configure.SqlBody
import io.edurt.datacap.common.sql.configure.SqlColumn
import io.edurt.datacap.common.sql.configure.SqlType
import io.edurt.datacap.executor.ExecutorService
import io.edurt.datacap.executor.common.RunState
import io.edurt.datacap.executor.configure.ExecutorRequest
import io.edurt.datacap.executor.configure.ExecutorResponse
import org.apache.commons.lang.StringEscapeUtils

class LocalExecutorService : ExecutorService
{
    override fun start(request: ExecutorRequest): ExecutorResponse
    {
        val response = ExecutorResponse()
        try
        {
            val input = request.input
            val output = request.output
            val inputPlugin = input.plugin
            if (inputPlugin != null)
            {
                val inputResult = inputPlugin.execute(input.originConfigure, input.query)

                if (inputResult.isSuccessful)
                {
                    val fullInsertStatement = Lists.newArrayList<String>()
                    response.count = inputResult.columns.size
                    inputResult.columns.forEach { it ->
                        val columns = Lists.newArrayList<SqlColumn>()
                        val objectNode: ObjectNode = it as ObjectNode
                        input.originColumns.forEach { value ->
                            columns.add(
                                SqlColumn.builder()
                                    .column(String.format("`%s`", value.name))
                                    .value(String.format("'%s'", StringEscapeUtils.escapeSql(objectNode.get(value.original).asText())))
                                    .build()
                            )
                        }
                        val body = SqlBody.builder()
                            .type(SqlType.INSERT)
                            .database(input.database)
                            .table(input.table)
                            .columns(columns)
                            .build()
                        fullInsertStatement.add(SqlBuilder(body).sql)
                    }

                    if (fullInsertStatement.isNotEmpty())
                    {
                        val outputPlugin = output.plugin
                        if (outputPlugin != null)
                        {
                            val outputResult = outputPlugin.execute(output.originConfigure, fullInsertStatement.joinToString("\n\n"))
                            if (! outputResult.isSuccessful)
                            {
                                throw RuntimeException(outputResult.message)
                            }
                            else
                            {
                                response.successful = true
                                response.state = RunState.SUCCESS
                            }
                        }
                        else
                        {
                            throw RuntimeException("Output plugin is null")
                        }
                    }
                    else
                    {
                        throw RuntimeException("No data to insert")
                    }
                }
                else
                {
                    throw RuntimeException(inputResult.message)
                }
            }
            else
            {
                throw RuntimeException("Input plugin is null")
            }
        }
        catch (ex: Exception)
        {
            response.successful = false
            response.state = RunState.FAILURE
            response.message = ex.message
        }
        return response
    }

    override fun stop(request: ExecutorRequest): ExecutorResponse
    {
        TODO("Not yet implemented")
    }
}
