SELECT [Ramac] as RAMAC
      ,[ActivityID] as ACTIVITYID
      ,[ActualFinish] as ACTUALFINISH
      ,[ActualHours] as ACTUALHOURS
      ,[ACC] as ETVS_ACC
      ,[ActualStart] as ACTUALSTART
      ,[AwardDate] as AWARDDATE
      ,[BidHours] as BIDHOURS
      ,[CAFCount] as CAFCOUNT
      ,[CAFHours] as CAFHOURS
      ,[Comp] as ETVS_COMP
      ,[ProductionNeedDate] as PRODUCTIONNEEDDATE
      ,[ControlCode] as CONTROLCODE
      ,[Customer] as CUSTOMER
      ,[CustomerControlLoad] as CUSTOMERCONTROLLOAD
      ,[CustomerNeed] as CUSTOMERNEED
      ,[CustomerEffectivity] as CUSTOMEREFFECTIVITY
      ,[Design] as DESIGN
      ,null as DESIGNSHOP
      ,[ECD] as ECD
      ,[ENGRChange] as ENGRCHANGE
      ,[ENGREffectivity] as ENGREFFECTIVITY
      ,[EstHrs] as ESTHRS
      ,[FileLoad] as FILELOAD
      ,[GeneralComments] as GENERALCOMMENTS
      ,[FormType] as FORMTYPE
      ,[ItemNumber] as ITEMNUMBER
      ,[InstructionNumber] as INSTRUCTIONNUMBER
      ,[IMP] as IMP
      ,[LinkOrders] as LINKORDERS
      ,[Notes] as NOTES
      ,[Offload] as OFFLOAD
      ,[Package] as ETVS_PACKAGE
      ,[PartNumber] as PARTNUMBER
      ,[PercentComplete] as PERCENTCOMPLETE
      ,[PDID] as PDID
      ,[PLGRP] as PLGRP
      ,[RCS] as RCS
      ,[Reason] as REASON
      ,[RecordHrsTotal] as RECORDHRSTOTAL
      ,[Region] as REGION
      ,[RelatedOrders] as RELATEDORDERS
      ,[Revisions] as REVISIONS
      ,[Shop] as SHOP
      ,[Shipping] as SHIPPING
      ,[Serial] as ETVS_SERIAL
      ,[SiteCode] as SITECODE
      ,[Source] as ETVS_SOURCE
      ,[SourceCode] as SOURCECODE
      ,[SplitNumber] as SPLITNUMBER
      ,[Start] as ETVS_START
      ,[Station] as STATION
      ,[StatusCode] as STATUSCODE
      ,[SupplierComp] as SUPPLIERCOMP
      ,[SupplierNotes] as SUPPLIERNOTES
      ,[SupplierShop] as SUPPLIERSHOP
      ,[SupplierStart] as SUPPLIERSTART
      ,[TrackNumber] as TRACKNUMBER
      ,[TTO] as TTO
      ,[ToolType] as TOOLTYPE
      ,[ToolReceived] as TOOLRECEIVED
      ,[ToolShipped] as TOOLSHIPPED
      ,[ToolSub] as TOOLSUB
      ,[ToolQty] as TOOLQTY
      ,[SOW] as SOW
      ,[UsingShop] as USINGSHOP
      ,[UpdatedFromTip] as UPDATEDFROMTIP
      ,[UnitNumber] as UNITNUMBER
      ,[ToBreakdown] as TOBREAKDOWN
      ,[ToMaterielDate] as TOMATERIELDATE
      ,[ToolDetailNumber] as TOOLDETAILNUMBER
      ,[TaskSupport] as TASKSUPPORT
      ,[WorkCenter] as WORKCENTER
      ,[WorkOrder] as WORKORDER
      ,[NWPLoad] as NWPLOAD
      ,[CurrEst] as CURREST
      ,[OrderSource] as ORDERSOURCE
      ,[TDRFocalBems] as TDRFOCALBEMS
      ,[Customer TE] as CUSTOMERTE
      ,[CustomerTEBkBems] as CUSTOMERTEBKBEMS
      ,[Customer TE BK] as CUSTOMERTEBK
      ,[PlannerBemsID] as PLANNERBEMSID
      ,[OperationID] as OPERATIONID
      ,[OperationStatus] as OPERATIONSTATUS
      ,[OperationStartSched] as OPERATIONSTARTSCHED
      ,[OperationCompSched] as OPERATIONCOMPSCHED
      ,[Customer TME] as CUSTOMERTME
      ,[CustomerTMEBk] as CUSTOMERTMEBK
      ,[Customer TME BK] as CUSTOMER_TME_BK
      ,[CustomerAnalyst] as CUSTOMERANALYST
      ,[Customer Focal] as CUSTOMERFOCAL
      ,[CustomerFocalBkBems] as CUSTOMERFOCALBKBEMS
      ,[Customer Focal BK] as CUSTOMERFOCALBK
      ,[CustomerCAMBems] as CUSTOMERCAMBEMS
      ,[CCustomer CAM] as CCUSTOMERCAM
      ,[CustomerCAMBkBems] as CUSTOMERCAMBKBEMS
      ,[Customer CAM BK] as CUSTOMERCAMBK
      ,[BrokerageBusOpRepBems] as BROKERAGEBUSOPREPBEMS
      ,[Brokerage Op Rep] as BROKERAGEOPREP
      ,[BrokerageBusOpRepBkBems] as BROKERAGEBUSOPREPBKBEMS
      ,[Brokerage Op Rep BK] as BROKERAGEOPREPBK
      ,[LSFAFocalBems] as LSFAFOCALBEMS
      ,[Brokerage TE/TME] as BROKERAGETE_TME
      ,[BrokerageTEBkBems] as BROKERAGETEBKBEMS
      ,[Brokerage TE/TME BK] as BROKERAGETE_TMEBK
      ,[AnalystRepBKBems] as ANALYSTREPBKBEMS
      ,[Brokerage Analyst BK] as BROKERAGEANALYSTBK
      ,[FieldrepBems] as FIELDREPBEMS
      ,[Field Rep/Assign To] as FIELDREP_ASSIGNTO
      ,[FieldRepBKBemsID] as FIELDREPBKBEMSID
      ,[Field Rep/Assign To BK] as FIELDREP_ASSIGNTOBK
      ,[IEFocalBemsID] as IEFOCALBEMSID
      ,[IE Focal] as IEFOCAL
      ,[IEBkFocalBemID] as IEBKFOCALBEMID
      ,[IE Focal BK] as IEFOCALBK
      ,[BuyerFocalBems] as BUYERFOCALBEMS
      ,[Caller/Buyer] as CALLER_BUYER
      ,[BuyerCallerBkBems] as BUYERCALLERBKBEMS
      ,[Caller/Buyer BK] as CALLER_BUYERBK
      ,[InspName] as INSPNAME
      ,[Inspection Rep] as INSPECTIONREP
      ,[InspBkBems] as INSPBKBEMS
      ,[Inspection Rep BK] as INSPECTIONREPBK
      ,[ModelCode] as MODELCODE
      ,[Fault] as FAULT
      ,[InspRequested] as INSPREQUESTED
      ,[OperationAssignTo] as OPERATIONASSIGNTO
      ,[TSDate] as TSDATE
      ,[OperationAssignToLead] as OPERATIONASSIGNTOLEAD
      ,[SBR] as SBR
      ,[AnalystRepBems] as ANALYSTREPBEMS
      ,[OrderType] as ORDERTYPE
      ,[RevisedSupplierCommitDate] as REVISEDSUPPLIERCOMMITDATE
      ,[CCL] as CCL
      ,[OrderFlowState] as ORDERFLOWSTATE
  FROM [NWPACTIVE].[dbo].[BCA_Active]