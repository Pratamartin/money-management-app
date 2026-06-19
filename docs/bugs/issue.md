2026-06-04 15:32:31.327 24459-24459 HomeViewModel           com.pratatec.moneymgtapp             D  load mes=6 ano=2026 | periodos isFailure=true err=Unexpected JSON token at offset 0: Expected start of the array '[', but had '{' instead at path: $
JSON input: {"detail":"Usuário não encontrado","code":"user_not_found"}
2026-06-04 15:33:35.031 24459-24459 HomeViewModel           com.pratatec.moneymgtapp             D  load mes=6 ano=2026 | periodos isFailure=false err=null
2026-06-04 15:33:35.031 24459-24459 HomeViewModel           com.pratatec.moneymgtapp             D  periodos count=0 ids=[]
2026-06-04 15:33:35.031 24459-24459 HomeViewModel           com.pratatec.moneymgtapp             D  periodo encontrado: null
2026-06-04 15:33:51.829 24459-24459 HomeViewModel           com.pratatec.moneymgtapp             D  load mes=6 ano=2026 | periodos isFailure=false err=null
2026-06-04 15:33:51.829 24459-24459 HomeViewModel           com.pratatec.moneymgtapp             D  periodos count=1 ids=[1]
2026-06-04 15:33:51.833 24459-24459 HomeViewModel           com.pratatec.moneymgtapp             D  periodo encontrado: Periodo(id=1, mes=6, ano=2026, saldoCarteira=1000.0, saldoDisponivelMes=750.0)
2026-06-04 15:33:51.915 24459-24459 HomeViewModel           com.pratatec.moneymgtapp             D  resumo isFailure=true err=Unexpected JSON token at offset 18: Expected quotation mark '"', but had '1' instead at path: $.saldo_carteira
JSON input: {"saldo_carteira":1000.0,"saldo_disponivel_mes":750.0,"total_gasto_mes":0.0,"limite_hoje":27.77777777777778} value=null
2026-06-04 15:33:52.027 24459-24459 HomeViewModel           com.pratatec.moneymgtapp             D  gastos isFailure=false count=0
