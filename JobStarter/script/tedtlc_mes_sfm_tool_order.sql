SELECT [OrderID]
      ,[ToolSeries_ToolCode_ToolNumber]
      ,[ToolNumber]
      ,[Series]
      ,[Unit]
      ,[PlanSubType]
      ,[DetailNumber]
      ,[ToolCode]
      ,[Program]
      ,[DateInActive]
      ,[MdayInactive]
      ,[OrderRevision]
      ,[RelatedOrderID]
      ,[Action]
      ,[ToolSerial]
      ,[SiteCD_LOC]
      ,[SiteCD_LOC_Description]
      ,[SiteCD_EMPL]
      ,[SiteCD_EMPL_Description]
      ,[UserID]
      ,[BemsID]
      ,[DesignIndicator]
      ,[OwnerSHIP]
      ,[CageCode]
      ,[CategoryCode]
      ,[ProjectID]
      ,[Alerts]
      ,[Item]
      ,[PartDesc]
      ,[OrderStatus]
      ,[OnHold]
      ,[RequiredQTY]
      ,[CompleteQTY]
      ,[ExternalORDER]
      ,[DATESTARTPLAN]
      ,[MdaySTARTPLAN]
      ,[DATESTARTACTL]
      ,[MdaySTARTACTL]
      ,[DATECOMPPLAN]
      ,[MdayCOMPPLAN]
      ,[DATECOMPACTL]
      ,[MdayCOMPACTL]
      ,[SPLITSTATE]
      ,[ORDERTYPE]
      ,[ORDERMAINTSTATUS]
      ,[INWORKORDERREV]
      ,[INMAINTSAVED]
      ,[PLANKEY]
      ,[OrderHoldCode]
      ,[NEWDATESTARTPLAN]
      ,[NEWMDaySTARTPLAN]
      ,[NEWDATECOMPPLAN]
      ,[NEWMDAYCOMPPLAN]
      ,[ORDERMAINTDOC]
      ,[SIGNOFFCYCLE]
      ,[SITECD_ORDER]
      ,[CRITICALRATIO]
      ,[TOTALSETUP]
      ,[TOTALRUN]
      ,[TotEstmHrs]
      ,[SumEstmWithDetails]
      ,[CHKCHANGEALERTS]
      ,[WORKPKGID]
      ,[PARTSHORTAGEFLAG]
      ,[ADDINFO1]
      ,[ADDINFO2]
      ,[ADDINFO3]
      ,[ADDINFO4]
      ,[ADDINFO5]
      ,[ORDERAVAILABLE]
      ,[ActETSHrs]
      ,[ActOtherHrs]
      ,[TotActHrs]
      ,[SumActWithDetails]
      ,[CREATIONDATE]
      ,[CREATIONMday]
      ,[ACCOUNTABLEFLAG]
      ,[CHANGEAUTHORITY]
      ,[DIVISION]
      ,[LOCATION]
      ,[PRODNEEDDATE]
      ,[PRODNEEDMday]
      ,[SCHCOMPLETEDATE]
      ,[SCHCOMPLETEMday]
      ,[SCHINWORKDATE]
      ,[SCHINWORKMday]
      ,[TOOLNAME]
      ,[REASONCODE]
      ,[COSTCODE]
      ,[GROUPCODE]
      ,[SOURCECODE]
      ,[SUBNUMBER]
      ,[USAGEPOINT]
      ,[ReworkSeq]
      ,[REVNUMBER]
      ,[TOOLDETAILNBR]
      ,[TOOLEFF]
      ,[TRTNR]
      ,[USINGDEPT]
      ,[CHANGENUMBER]
      ,[DATEONHOLD]
      ,[MdayOnHold]
      ,[HOLDCODE]
      ,[MESTimeStamp]
      ,[MESStageLoadDate]
      ,[TEDLoadDate]
      ,[ACTIVITYID]
      ,[SFM_TSTDSHOURS]
      ,[SFM_TOOLEFF]
      ,[SFM_TCO]
      ,[SFM_RESPONSPVSR]
      ,[SFM_TD]
  FROM [TEDTLC].[dbo].[MES_SFM_ToolOrder]
 WHERE [TEDTLC].[dbo].[MES_SFM_ToolOrder].[SiteCD_LOC] = 'WA016'