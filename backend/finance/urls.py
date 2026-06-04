from django.urls import path
from rest_framework.routers import DefaultRouter

from .views import CategoriaViewSet, GastoDiarioViewSet, PeriodoViewSet

router = DefaultRouter()
router.register("categorias", CategoriaViewSet, basename="categoria")
router.register("periodos", PeriodoViewSet, basename="periodo")

gastos_list = GastoDiarioViewSet.as_view({"get": "list", "post": "create"})
gastos_detail = GastoDiarioViewSet.as_view({"patch": "partial_update", "delete": "destroy"})

urlpatterns = router.urls + [
    path("periodos/<int:periodo_id>/gastos-diarios/", gastos_list, name="gastos-diarios-list"),
    path("periodos/<int:periodo_id>/gastos-diarios/<int:pk>/", gastos_detail, name="gastos-diarios-detail"),
]
