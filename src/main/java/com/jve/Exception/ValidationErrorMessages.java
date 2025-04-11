package com.jve.Exception;

/**
 * Clase que centraliza los mensajes de error de validación de la aplicación
 */
public class ValidationErrorMessages {
    // Mensajes para Producto
    public static final String PRODUCTO_NOMBRE_REQUERIDO = "El nombre es obligatorio";
    public static final String PRODUCTO_DESCRIPCION_REQUERIDA = "La descripción es obligatoria";
    public static final String PRODUCTO_PRECIO_REQUERIDO = "El precio es obligatorio";
    public static final String PRODUCTO_PRECIO_POSITIVO = "El precio debe ser un número positivo";
    public static final String PRODUCTO_STOCK_REQUERIDO = "El stock es obligatorio";
    public static final String PRODUCTO_STOCK_MINIMO = "El stock debe ser un número entero igual o mayor que 0";
    public static final String PRODUCTO_YA_EXISTE = "Ya existe un producto con este nombre";
    
    // Mensajes para Categoria
    public static final String CATEGORIA_NOMBRE_REQUERIDO = "El nombre de la categoría es obligatorio";
    public static final String CATEGORIA_DESCRIPCION_REQUERIDA = "La descripción de la categoría es obligatoria";
    public static final String CATEGORIA_NOMBRE_DUPLICADO = "Ya existe una categoría con este nombre";
    public static final String CATEGORIA_NO_ENCONTRADA = "No se encontró la categoría con id: %d";
    public static final String CATEGORIA_PROTEGIDA = "No se puede crear una categoría con el nombre '%s' porque es un nombre protegido del sistema.\\nEsta categoría está reservada para productos sin clasificar y se gestiona automáticamente.";
    public static final String CATEGORIA_FORZAR_MOVIMIENTO = "Cuando se especifican productos existentes, el campo 'forzarMovimiento' debe estar explícitamente definido como true o false";
    public static final String CATEGORIA_PRODUCTOS_NO_EXISTEN = "Algunos IDs de productos no existen";

    // Mensajes para Autenticación
    public static final String AUTH_EMAIL_REQUERIDO = "El email es obligatorio";
    public static final String AUTH_EMAIL_FORMATO = "El email debe tener un formato válido";
    public static final String AUTH_EMAIL_YA_REGISTRADO = "El email ya está registrado";
    public static final String AUTH_PASSWORD_REQUERIDO = "La contraseña es obligatoria";
    public static final String AUTH_PASSWORD_FORMATO = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial";
    public static final String AUTH_PASSWORD_LONGITUD = "La contraseña debe tener al menos 8 caracteres";
    public static final String AUTH_CREDENCIALES_INCORRECTAS = "Credenciales incorrectas";
    public static final String AUTH_USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    public static final String AUTH_NOMBRE_REQUERIDO = "El nombre es obligatorio";
    public static final String AUTH_APELLIDOS_REQUERIDOS = "Los apellidos son obligatorios";
    public static final String AUTH_DIRECCION_REQUERIDA = "La dirección es obligatoria";
    public static final String AUTH_TELEFONO_REQUERIDO = "El teléfono es obligatorio";
} 