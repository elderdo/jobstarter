SELECT o.[ORDERID]
      ,o.[OPERATIONID]
      ,o.[INSPTYPE]
      ,o.[OperNo]
      ,o.[STATUS]
      ,o.[ACTION]
      ,o.[ONHOLD]
      ,o.[HoldCode]
      ,o.[DateOnHold]
      ,o.[MDAYOnHold]
      ,o.[WORKLOCID]
      ,o.[DATESTARTPLAN]
      ,o.[MDAYSTARTPLAN]
      ,o.[DATESTARTACTL]
      ,o.[MDAYSTARTACTL]
      ,o.[DATECOMPPLAN]
      ,o.[MDAYCOMPPLAN]
      ,o.[DATECOMPACTL]
      ,o.[MDAYCOMPACTL]
      ,o.[InProcess_Status]
      ,o.[SETUPTIME]
      ,o.[TOTALRUNTIME]
      ,o.[TotEstmOp]
      ,o.[STARTEDBY]
      ,o.[StartedByBemsID]
      ,o.[StartedByName]
      ,o.[AssignedTo]
      ,o.[AssignedToID]
      ,o.[AssignedToName]
      ,o.[COMPLETEBY]
      ,o.[CompleteByBemsID]
      ,o.[CompleteByName]
      ,o.[ACTIVENCR]
      ,o.[PRIORITYCODE]
      ,o.[DATEPROMISED]
      ,o.[MDAYPromised]
      ,o.[REVISION]
      ,o.[REVDATE]
      ,o.[RevMDAY]
      ,o.[PLANOPERID]
      ,o.[SUBSTATUSDESC]
      ,o.[PRIORITYSORDORDER]
      ,o.[DESCRIPTION]
      ,o.[ADDMODFLAG]
      ,o.[BASEOPER]
      ,o.[ADDOPINFO1]
      ,o.[ADDOPINFO2]
      ,o.[ADDOPINFO3]
      ,o.[ADDOPINFO4]
      ,o.[ADDOPINFO5]
      ,o.[OPERAVAILABLE]
      ,o.[OPERFLAGS]
      ,o.[WorkCenter]
      ,o.[WorkArea]
      ,o.[WorkLoc]
      ,o.[Loan]
      ,o.[WorkLocationLoadDate]
      ,o.[WorkLocationLoadMDAY]
      ,o.[COMPLETEQTY]
      ,o.[READYQTY]
      ,o.[REJECTQTY]
      ,o.[Incoming_Ready]
      ,o.[SystemLoadDate]
  FROM [TEDTLC].[dbo].[MES_SFM_ToolOrder_Complete] as t
  JOIN [TEDTLC].[dbo].[MES_SFM_OperationData_Complete] as o
    ON (o.[ORDERID] = t.[ORDERID])
 WHERE t.[SiteCD_LOC] = 'WA016'