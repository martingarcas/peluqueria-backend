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
    public static final String PRODUCTO_NO_CAMBIOS = "No se detectaron cambios en el producto";

    // Mensajes para Categoria
    public static final String CATEGORIA_NOMBRE_REQUERIDO = "El nombre de la categoría es obligatorio";
    public static final String CATEGORIA_DESCRIPCION_REQUERIDA = "La descripción de la categoría es obligatoria";
    public static final String CATEGORIA_NOMBRE_DUPLICADO = "Ya existe una categoría con este nombre";
    public static final String CATEGORIA_NO_ENCONTRADA = "No se encontró la categoría con id: %d";
    public static final String CATEGORIA_PROTEGIDA = "No se puede crear una categoría con el nombre '%s' porque es un nombre protegido del sistema. Esta categoría está reservada para productos sin clasificar y se gestiona automáticamente.";
    public static final String CATEGORIA_PROTEGIDA_ELIMINAR = "No se puede eliminar la categoría '%s' porque es un nombre protegido del sistema. Esta categoría está reservada para productos sin clasificar y se gestiona automáticamente.";
    public static final String CATEGORIA_FORZAR_MOVIMIENTO = "Cuando se especifican productos existentes, el campo 'forzarMovimiento' debe estar explícitamente definido como true o false";
    public static final String CATEGORIA_PRODUCTOS_NO_EXISTEN = "Algunos IDs de productos no existen";

    // Nuevos mensajes para Categoria
    public static final String CATEGORIA_PRODUCTOS_CREADOS = "Se crearon %d productos nuevos en la categoría";
    public static final String CATEGORIA_PRODUCTOS_CON_CATEGORIA = "Algunos productos ya tienen categoría asignada: %s. Marca 'forzarMovimiento' como true para moverlos a esta categoría.";
    public static final String CATEGORIA_PRODUCTO_MOVIDO = "Producto existente '%s' movido desde '%s'";
    public static final String CATEGORIA_PROTEGIDA_NO_EXISTE = "La categoría protegida no existe";
    public static final String CATEGORIA_PRODUCTOS_MANTIENEN = "Productos que mantienen su categoría actual:";
    public static final String CATEGORIA_PRODUCTOS_ASIGNADOS = "Productos existentes asignados a la nueva categoría:";
    public static final String CATEGORIA_SIN_PRODUCTOS = "sin productos asociados";
    public static final String CATEGORIA_SOLO_SIN_CATEGORIA = "Solo se asignaron los productos existentes que no tenían categoría";
    public static final String CATEGORIA_NO_ASIGNADOS = "No se asignaron productos existentes ya que todos pertenecen a otras categorías";
    public static final String CATEGORIA_PRODUCTOS_ASOCIADOS = "La categoría tiene productos asociados. Debes especificar 'eliminarProductos=true' para eliminar los productos junto con la categoría, o 'eliminarProductos=false' para moverlos a la categoría 'Otros productos'";

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
    public static final String AUTH_NO_PERMISOS = "No tienes permisos para realizar esta acción";
    public static final String AUTH_NO_PERMISOS_ROL = "No tienes permisos para cambiar el rol del usuario";
    public static final String AUTH_NO_PERMISOS_ELIMINAR = "Los trabajadores no pueden eliminar su cuenta";
    public static final String AUTH_ROL_NO_EXISTE = "El rol especificado no existe. Los roles válidos son: admin, trabajador, cliente";
    public static final String AUTH_ROL_REQUERIDO = "El rol es obligatorio";

    // Mensajes para Estado
    public static final String ESTADO_NO_ENCONTRADO = "Estado no encontrado";
    public static final String ESTADO_YA_EXISTE = "Ya existe un estado con ese nombre y tipo";
    public static final String ESTADO_NOMBRE_REQUERIDO = "El nombre del estado es obligatorio";
    public static final String ESTADO_TIPO_REQUERIDO = "El tipo de estado es obligatorio";

    // Mensajes para Usuario
    public static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    public static final String USUARIO_NO_ES_TRABAJADOR = "El usuario no es un trabajador";
    public static final String TRABAJADOR_SIN_CONTRATO = "El trabajador debe tener un contrato asignado";
    public static final String TRABAJADOR_SIN_CONTRATO_ACTIVO = "El trabajador no tiene un contrato activo";
    public static final String TRABAJADOR_SIN_SERVICIOS = "El trabajador debe tener al menos un servicio asignado";
    public static final String TRABAJADOR_SIN_HORARIOS = "El trabajador debe tener al menos un horario asignado";
    public static final String FOTO_NO_ENCONTRADA = "No se encontró la foto del usuario";
    
    // Mensajes para Contrato
    public static final String CONTRATO_NO_ENCONTRADO = "Contrato no encontrado";
    public static final String CONTRATO_TEMPORAL_REQUIERE_FECHA_FIN = "Los contratos temporales requieren una fecha de finalización";
    public static final String CONTRATO_FIJO_NO_FECHA_FIN = "Los contratos fijos no deben tener fecha de finalización";
    public static final String CONTRATO_FECHA_FIN_ANTERIOR_INICIO = "La fecha de fin no puede ser anterior a la fecha de inicio";
    public static final String CONTRATOS_USUARIO_RECUPERADOS = "Contratos del usuario recuperados con éxito";
    public static final String CONTRATO_YA_EXISTE = "El usuario ya tiene un contrato activo o pendiente";
    public static final String CONTRATO_FECHA_INICIO_PASADA = "La fecha de inicio no puede ser una fecha pasada";
    public static final String CONTRATO_SALARIO_NEGATIVO = "El salario no puede ser un valor negativo";
    public static final String CONTRATO_SALARIO_REQUERIDO = "El salario es obligatorio";
    public static final String CONTRATO_TIPO_NO_VALIDO = "El tipo de contrato no es válido. Los tipos válidos son: fijo, temporal";
    public static final String CONTRATO_TIPO_REQUERIDO = "El tipo de contrato es obligatorio";
    public static final String CONTRATO_FECHA_INICIO_REQUERIDA = "La fecha de inicio es obligatoria";
    public static final String CONTRATO_DOCUMENTO_REQUERIDO = "El documento del contrato es requerido";

    // Mensajes para Horario
    public static final String HORARIO_NO_ENCONTRADO = "El horario no existe";
    public static final String HORARIO_SOLAPAMIENTO = "El horario se solapa con otro existente";
    public static final String HORARIO_HORA_FIN_ANTERIOR = "La hora de fin debe ser posterior a la hora de inicio";
    public static final String HORARIO_DIA_REQUERIDO = "El día de la semana es obligatorio";
    public static final String HORARIO_HORA_INICIO_REQUERIDA = "La hora de inicio es obligatoria";
    public static final String HORARIO_HORA_FIN_REQUERIDA = "La hora de fin es obligatoria";
    public static final String HORARIO_NOMBRE_REQUERIDO = "El nombre del horario es obligatorio";
    public static final String DIA_SEMANA_INVALIDO = "El día de la semana no es válido. Valores permitidos: lunes, martes, miércoles, jueves, viernes, sábado, domingo";
    public static final String ERROR_FORMATO_JSON = "Error en el formato de los datos enviados";
    public static final String HORARIOS_NO_ENCONTRADOS = "Uno o más horarios no fueron encontrados";

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

    // Mensajes para Trabajador
    public static final String TRABAJADOR_FOTO_REQUERIDA = "La foto del trabajador es requerida";
    public static final String TRABAJADOR_CONTRATO_REQUERIDO = "El contrato del trabajador es requerido";
    public static final String TRABAJADOR_SERVICIOS_REQUERIDOS = "Debe seleccionar al menos un servicio";
    public static final String TRABAJADOR_HORARIOS_REQUERIDOS = "Debe seleccionar al menos un horario";
    public static final String ERROR_VALIDACION = "Error de validación";

    // Mensajes de validación para Usuario
    public static final String ERROR_USUARIO_EXISTENTE = "Ya existe un usuario con ese email";
    public static final String ERROR_ROL_INVALIDO = "El rol especificado no es válido";
    public static final String ERROR_CONTRASENA_INVALIDA = "La contraseña debe tener al menos 6 caracteres";
    public static final String ERROR_EMAIL_INVALIDO = "El formato del email no es válido";
    public static final String ERROR_NOMBRE_REQUERIDO = "El nombre es requerido";
    public static final String ERROR_APELLIDO_REQUERIDO = "El apellido es requerido";
    public static final String ERROR_EMAIL_REQUERIDO = "El email es requerido";
    public static final String ERROR_CONTRASENA_REQUERIDA = "La contraseña es requerida";
    public static final String ERROR_ROL_REQUERIDO = "El rol es requerido";

    // Mensajes para Cita
    public static final String CITA_SERVICIO_REQUERIDO = "El servicio es obligatorio";
    public static final String CITA_TRABAJADOR_REQUERIDO = "El trabajador es obligatorio";
    public static final String CITA_FECHA_REQUERIDA = "La fecha es obligatoria";
    public static final String CITA_FECHA_PASADA = "La fecha no puede ser anterior a hoy";
    public static final String CITA_HORA_REQUERIDA = "La hora es obligatoria";
    public static final String CITA_HORA_INVALIDA = "La hora debe estar dentro del horario del trabajador";
    public static final String CITA_SOLAPAMIENTO = "El trabajador ya tiene una cita en ese horario";
    public static final String CITA_USUARIO_SOLAPAMIENTO = "Ya tienes una cita programada en ese horario";
    public static final String CITA_DURACION_EXCEDE = "La duración del servicio excede el horario disponible";
    public static final String CITA_TRABAJADOR_NO_DISPONIBLE = "El trabajador no está disponible en ese horario";
    public static final String CITA_TRABAJADOR_NO_SERVICIO = "El trabajador no ofrece este servicio";
    public static final String CITA_NO_ENCONTRADA = "No se encontró la cita";
    public static final String CITA_NO_PERMISOS = "No tienes permisos para gestionar esta cita";
    public static final String CITA_ESTADO_INVALIDO = "El estado de la cita no es válido";
    public static final String CITA_YA_CANCELADA = "La cita ya está cancelada";
    public static final String CITA_YA_COMPLETADA = "La cita ya está completada";
    public static final String CITA_LISTA_VACIA = "La lista de citas no puede estar vacía";

    // Mensajes para Archivo
    public static final String ARCHIVO_NO_ENCONTRADO = "El archivo solicitado no fue encontrado";
} 