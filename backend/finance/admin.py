from django.contrib import admin
from .models import Categoria, GastoDiario, Periodo

admin.site.register(Categoria)
admin.site.register(Periodo)
admin.site.register(GastoDiario)
