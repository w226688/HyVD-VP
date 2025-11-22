package io.edurt.datacap.condor.metadata;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.condor.manager.TableManager;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class DatabaseDefinition
{
    private String name;
    private Path path;
    private TableManager tableManager;

    public DatabaseDefinition(String name, Path path)
    {
        this.name = name;
        this.path = path;
        this.tableManager = new TableManager(path);
    }
}
