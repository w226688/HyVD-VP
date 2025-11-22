<cfoutput>
    <h1>Running #Server.coldfusion.productname# #Server.lucee.version# @ #getTickCount()#</h1>

    <h2>{lucee-server}: #expandPath("{lucee-server}")#</h2>
    <h2>Web root      : #expandPath("/")#</h2>
    <h2>Template path : #getCurrentTemplatePath()#</h2>
</cfoutput>
