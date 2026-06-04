from django.shortcuts import get_object_or_404
from rest_framework import mixins, permissions, viewsets
from rest_framework.decorators import action
from rest_framework.exceptions import PermissionDenied, ValidationError
from rest_framework.response import Response

from .models import Categoria, GastoDiario, Periodo
from .serializers import CategoriaSerializer, GastoDiarioSerializer, PeriodoSerializer
from .services import calcular_limite_diario, calcular_total_gasto_mes


class CategoriaViewSet(
    mixins.ListModelMixin,
    mixins.CreateModelMixin,
    mixins.DestroyModelMixin,
    viewsets.GenericViewSet,
):
    serializer_class = CategoriaSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return Categoria.objects.filter(
            usuario__isnull=True
        ) | Categoria.objects.filter(usuario=self.request.user)

    def perform_create(self, serializer):
        serializer.save(tipo=Categoria.Tipo.CUSTOM, usuario=self.request.user)

    def perform_destroy(self, instance):
        if instance.tipo == Categoria.Tipo.DEFAULT:
            raise ValidationError("Categorias default não podem ser removidas.")
        if instance.usuario != self.request.user:
            raise PermissionDenied
        instance.delete()


class PeriodoViewSet(
    mixins.ListModelMixin,
    mixins.CreateModelMixin,
    viewsets.GenericViewSet,
):
    serializer_class = PeriodoSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return Periodo.objects.filter(usuario=self.request.user)

    @action(detail=True, methods=["get"])
    def resumo(self, request, pk=None):
        periodo = self.get_object()
        return Response({
            "saldo_carteira": str(periodo.saldo_carteira),
            "saldo_disponivel_mes": str(periodo.saldo_disponivel_mes),
            "total_gasto_mes": str(calcular_total_gasto_mes(periodo)),
            "limite_hoje": str(calcular_limite_diario(periodo)),
        })


class GastoDiarioViewSet(
    mixins.ListModelMixin,
    mixins.CreateModelMixin,
    mixins.UpdateModelMixin,
    mixins.DestroyModelMixin,
    viewsets.GenericViewSet,
):
    serializer_class = GastoDiarioSerializer
    permission_classes = [permissions.IsAuthenticated]
    http_method_names = ["get", "post", "patch", "delete", "head", "options"]

    def _get_periodo(self):
        return get_object_or_404(
            Periodo,
            pk=self.kwargs["periodo_id"],
            usuario=self.request.user,
        )

    def get_queryset(self):
        return GastoDiario.objects.filter(
            periodo_id=self.kwargs["periodo_id"],
            periodo__usuario=self.request.user,
        )

    def get_serializer_context(self):
        context = super().get_serializer_context()
        context["periodo"] = self._get_periodo()
        return context

    def perform_create(self, serializer):
        serializer.save(periodo=self._get_periodo())
