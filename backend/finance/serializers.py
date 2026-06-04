from rest_framework import serializers
from .models import Categoria, GastoDiario, Periodo


class CategoriaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Categoria
        fields = ["id", "nome", "tipo"]
        read_only_fields = ["tipo"]


class PeriodoSerializer(serializers.ModelSerializer):
    class Meta:
        model = Periodo
        fields = ["id", "mes", "ano", "saldo_carteira", "saldo_disponivel_mes"]

    def create(self, validated_data):
        validated_data["usuario"] = self.context["request"].user
        return super().create(validated_data)


class GastoDiarioSerializer(serializers.ModelSerializer):
    categoria = CategoriaSerializer(read_only=True)
    categoria_id = serializers.PrimaryKeyRelatedField(
        queryset=Categoria.objects.all(),
        source="categoria",
        write_only=True,
    )

    class Meta:
        model = GastoDiario
        fields = ["id", "data", "valor", "descricao", "categoria", "categoria_id"]

    def validate(self, attrs):
        periodo = self.context["periodo"]
        data = attrs.get("data")
        if data and (data.month != periodo.mes or data.year != periodo.ano):
            raise serializers.ValidationError(
                {"data": "A data não pertence ao mês/ano do período."}
            )
        return attrs
