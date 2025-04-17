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

    // Mensajes para Estado
    public static final String ESTADO_NO_ENCONTRADO = "Estado no encontrado";
    public static final String ESTADO_YA_EXISTE = "Ya existe un estado con ese nombre y tipo";

    // Mensajes para Usuario
    public static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    public static final String USUARIO_NO_ES_TRABAJADOR = "El usuario no es un trabajador";
    
    // Mensajes para Contrato
    public static final String CONTRATO_NO_ENCONTRADO = "Contrato no encontrado";
    public static final String CONTRATO_TEMPORAL_REQUIERE_FECHA_FIN = "Un contrato temporal requiere fecha de finalización";
    public static final String CONTRATO_FIJO_NO_FECHA_FIN = "Un contrato fijo no debe tener fecha de finalización";
    public static final String CONTRATO_FECHA_FIN_ANTERIOR_INICIO = "La fecha de fin no puede ser anterior a la fecha de inicio";
    public static final String CONTRATOS_USUARIO_RECUPERADOS = "Contratos del usuario recuperados con éxito";
    public static final String CONTRATO_YA_EXISTE = "El usuario ya tiene un contrato activo o pendiente";
    public static final String CONTRATO_FECHA_INICIO_PASADA = "La fecha de inicio no puede ser una fecha pasada";

    // Mensajes para Horario
    public static final String HORARIO_NO_ENCONTRADO = "El horario no existe";
    public static final String HORARIO_SOLAPAMIENTO = "El horario se solapa con otro existente";
    public static final String HORARIO_HORA_FIN_ANTERIOR = "La hora de fin debe ser posterior a la hora de inicio";
    public static final String HORARIO_DIA_REQUERIDO = "El día de la semana es obligatorio";
    public static final String HORARIO_HORA_INICIO_REQUERIDA = "La hora de inicio es obligatoria";
    public static final String HORARIO_HORA_FIN_REQUERIDA = "La hora de fin es obligatoria";
    public static final String DIA_SEMANA_INVALIDO = "El día de la semana no es válido. Valores permitidos: lunes, martes, miércoles, jueves, viernes, sábado, domingo";
    public static final String ERROR_FORMATO_JSON = "Error en el formato de los datos enviados";

    // Mensajes para Servicio
    public static final String SERVICIO_NOMBRE_REQUERIDO = "El nombre del servicio es obligatorio";
    public static final String SERVICIO_DESCRIPCION_REQUERIDA = "La descripción del servicio es obligatoria";
    public static final String SERVICIO_DURACION_REQUERIDA = "La duración del servicio es obligatoria";
    public static final String SERVICIO_DURACION_POSITIVA = "La duración debe ser un número positivo";
    public static final String SERVICIO_PRECIO_REQUERIDO = "El precio del servicio es obligatorio";
    public static final String SERVICIO_PRECIO_POSITIVO = "El precio debe ser un número positivo";
    public static final String SERVICIO_NO_ENCONTRADO = "No se encontró el servicio con id: %d";
    public static final String SERVICIO_NOMBRE_DUPLICADO = "Ya existe un servicio con este nombre";
    public static final String SERVICIO_NO_CAMBIOS = "No se detectaron cambios en el servicio";
    public static final String SERVICIOS_NO_ENCONTRADOS = "Uno o más servicios no fueron encontrados";
} 