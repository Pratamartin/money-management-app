from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from .models import User


@admin.register(User)
class CustomUserAdmin(UserAdmin):
    list_display = ("email", "username", "nome", "is_staff")
    search_fields = ("email", "username", "nome")
    ordering = ("email",)
    fieldsets = UserAdmin.fieldsets + (
        (None, {"fields": ("nome",)}),
    )
    add_fieldsets = UserAdmin.add_fieldsets + (
        (None, {"fields": ("email", "nome")}),
    )
