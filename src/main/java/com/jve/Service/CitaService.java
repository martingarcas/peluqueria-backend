package com.jve.Service;

import com.jve.Converter.CitaConverter;
import com.jve.DTO.CitaDTO;
import com.jve.Entity.*;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.Time;
import java.text.ParseException;

@Service
@RequiredArgsConstructor
public class CitaService {
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final EstadoRepository estadoRepository;
    private final CitaConverter citaConverter;
    private final ContratoRepository contratoRepository;
    
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerCitasPorUsuario() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
            
        List<Cita> citas;
        if (usuario.getRol().equals(RolUsuario.admin)) {
            citas = citaRepository.findAll();
        } else if (usuario.getRol().equals(RolUsuario.trabajador)) {
            citas = citaRepository.findByTrabajadorId(usuario.getId());
        } else {
            citas = citaRepository.findByUsuarioId(usuario.getId());
        }
        
        List<CitaDTO.CitaRequest> citasDTO = citaConverter.toDtoList(citas);
        CitaDTO response = new CitaDTO();
        response.setCitas(citasDTO);
            
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("mensaje", "Citas recuperadas exitosamente");
        responseMap.put("citas", response);
        return responseMap;
    }
    
    @Transactional
    public Map<String, Object> crearCita(CitaDTO citaDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
            
        // Configurar zona horaria para todas las citas
        configurarZonaHorariaCitas(citaDTO);
            
        List<Cita> citasCreadas = new ArrayList<>();
        
        for (CitaDTO.CitaRequest citaRequest : citaDTO.getCitas()) {
            Usuario trabajador = usuarioRepository.findById(citaRequest.getTrabajadorId())
                .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
                
            Servicio servicio = servicioRepository.findById(citaRequest.getServicioId())
                .orElseThrow(() -> new RuntimeException(
                    String.format(ValidationErrorMessages.SERVICIO_NO_ENCONTRADO, citaRequest.getServicioId())));
                    
            // Validar que el trabajador tenga el servicio asociado
            if (trabajador.getServicios().stream().noneMatch(s -> s.getId().equals(servicio.getId()))) {
                throw new RuntimeException(String.format(ValidationErrorMessages.CITA_TRABAJADOR_NO_SERVICIO, 
                    trabajador.getId(), servicio.getId()));
            }
                
            // Ajustar la zona horaria correctamente
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
            cal.setTime(citaRequest.getHoraInicio());
            
            // Convertir a Time para hora inicio
            Time horaInicio = Time.valueOf(String.format("%02d:%02d:00", 
                cal.get(Calendar.HOUR_OF_DAY), 
                cal.get(Calendar.MINUTE)));
            citaRequest.setHoraInicio(horaInicio);
            
            // Calcular hora fin
            cal.add(Calendar.MINUTE, servicio.getDuracion());
            Time horaFin = Time.valueOf(String.format("%02d:%02d:00", 
                cal.get(Calendar.HOUR_OF_DAY), 
                cal.get(Calendar.MINUTE)));
            citaRequest.setHoraFin(horaFin);
                
            validarDisponibilidad(trabajador, citaRequest, servicio);
            
            Estado estadoProgramada = estadoRepository.findByNombreAndTipoEstado("PROGRAMADA", TipoEstado.CITA)
                .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.ESTADO_NO_ENCONTRADO));
            
            Cita cita = citaConverter.toEntity(citaRequest, usuario, trabajador, servicio, estadoProgramada);
            citasCreadas.add(citaRepository.save(cita));
        }
        
        List<CitaDTO.CitaRequest> citasDTO = citaConverter.toDtoList(citasCreadas);
        CitaDTO response = new CitaDTO();
        response.setCitas(citasDTO);
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("mensaje", "Citas creadas exitosamente");
        responseMap.put("citas", response);
        return responseMap;
    }
    
    private void validarDisponibilidad(Usuario trabajador, CitaDTO.CitaRequest citaDTO, Servicio servicio) {
        // Validar que el trabajador tenga un contrato activo
        if (!contratoRepository.existsByUsuarioIdAndEstadoNombre(trabajador.getId(), "ACTIVO")) {
            throw new RuntimeException(ValidationErrorMessages.TRABAJADOR_SIN_CONTRATO_ACTIVO);
        }

        // Convertir la hora de inicio a Calendar para manipulación
        Calendar calInicio = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
        calInicio.setTime(citaDTO.getHoraInicio());
        calInicio.set(Calendar.SECOND, 0);
        calInicio.set(Calendar.MILLISECOND, 0);
        Date horaInicio = calInicio.getTime();

        // Calcular hora de fin
        Calendar calFin = (Calendar) calInicio.clone();
        calFin.add(Calendar.MINUTE, servicio.getDuracion());
        Date horaFin = calFin.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        System.out.println("Validando disponibilidad para:");
        System.out.println("Fecha: " + citaDTO.getFecha());
        System.out.println("Hora inicio: " + sdf.format(horaInicio));
        System.out.println("Hora fin: " + sdf.format(horaFin));

        // Validar que la cita esté dentro del horario del trabajador
        String diaSemana = new SimpleDateFormat("EEEE", new Locale("es", "ES"))
                .format(citaDTO.getFecha()).toLowerCase();

        boolean horarioValido = trabajador.getHorarios().stream()
                .filter(h -> h.getDiaSemana().name().toLowerCase().equals(diaSemana))
                .anyMatch(horario -> {
                    Calendar calHorarioInicio = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
                    calHorarioInicio.setTime(horario.getHoraInicio());
                    Calendar calHorarioFin = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
                    calHorarioFin.setTime(horario.getHoraFin());

                    int horaInicioMinutos = calInicio.get(Calendar.HOUR_OF_DAY) * 60 + calInicio.get(Calendar.MINUTE);
                    int horaFinMinutos = calFin.get(Calendar.HOUR_OF_DAY) * 60 + calFin.get(Calendar.MINUTE);
                    int horarioInicioMinutos = calHorarioInicio.get(Calendar.HOUR_OF_DAY) * 60 + calHorarioInicio.get(Calendar.MINUTE);
                    int horarioFinMinutos = calHorarioFin.get(Calendar.HOUR_OF_DAY) * 60 + calHorarioFin.get(Calendar.MINUTE);

                    System.out.println("Comparando con horario del trabajador:");
                    System.out.println("Horario inicio: " + sdf.format(horario.getHoraInicio()));
                    System.out.println("Horario fin: " + sdf.format(horario.getHoraFin()));
                    System.out.println("Hora inicio cita (minutos): " + horaInicioMinutos);
                    System.out.println("Hora fin cita (minutos): " + horaFinMinutos);
                    System.out.println("Horario inicio (minutos): " + horarioInicioMinutos);
                    System.out.println("Horario fin (minutos): " + horarioFinMinutos);

                    return horaInicioMinutos >= horarioInicioMinutos && horaFinMinutos <= horarioFinMinutos;
                });

        if (!horarioValido) {
            throw new RuntimeException(ValidationErrorMessages.CITA_TRABAJADOR_NO_DISPONIBLE);
        }

        // Validar solapamiento con otras citas
        List<Cita> citasSolapadas = citaRepository.findSolapadas(
                trabajador.getId(),
                citaDTO.getFecha(),
                horaInicio,
                horaFin,
                true
        );

        if (!citasSolapadas.isEmpty()) {
            System.out.println("Citas solapadas encontradas: " + citasSolapadas.size());
            citasSolapadas.forEach(c -> {
                System.out.println("Cita solapada:");
                System.out.println("  - Inicio: " + sdf.format(c.getHoraInicio()));
                System.out.println("  - Fin: " + sdf.format(c.getHoraFin()));
            });
            throw new RuntimeException(ValidationErrorMessages.CITA_SOLAPAMIENTO);
        }

        // Validar que el usuario no tenga otra cita en el mismo horario
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));

        citasSolapadas = citaRepository.findSolapadas(
                usuario.getId(),
                citaDTO.getFecha(),
                horaInicio,
                horaFin,
                false
        );

        if (!citasSolapadas.isEmpty()) {
            System.out.println("Usuario tiene citas solapadas: " + citasSolapadas.size());
            citasSolapadas.forEach(c -> {
                System.out.println("Cita solapada del usuario:");
                System.out.println("  - Inicio: " + sdf.format(c.getHoraInicio()));
                System.out.println("  - Fin: " + sdf.format(c.getHoraFin()));
            });
            throw new RuntimeException(ValidationErrorMessages.CITA_USUARIO_SOLAPAMIENTO);
        }
    }
    
    @Transactional
    public Map<String, Object> actualizarEstadoCita(Integer citaId, String nuevoEstado) {
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.CITA_NO_ENCONTRADA));
            
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
            
        if (!usuario.getRol().equals(RolUsuario.admin) && 
            !cita.getTrabajador().getId().equals(usuario.getId())) {
            throw new RuntimeException(ValidationErrorMessages.CITA_NO_PERMISOS);
        }
        
        if (cita.getEstado().getNombre().equals("CANCELADA")) {
            throw new RuntimeException(ValidationErrorMessages.CITA_YA_CANCELADA);
        }
        if (cita.getEstado().getNombre().equals("COMPLETADA")) {
            throw new RuntimeException(ValidationErrorMessages.CITA_YA_COMPLETADA);
        }
        
        Estado nuevoEstadoEntity = estadoRepository.findByNombreAndTipoEstado(nuevoEstado, TipoEstado.CITA)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.ESTADO_NO_ENCONTRADO));
            
        cita.setEstado(nuevoEstadoEntity);
        cita = citaRepository.save(cita);
        
        CitaDTO.CitaRequest citaDTO = citaConverter.toDto(cita);
        CitaDTO response = new CitaDTO();
        response.setCitas(Collections.singletonList(citaDTO));
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("mensaje", "Estado de la cita actualizado exitosamente");
        responseMap.put("cita", response);
        return responseMap;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDisponibilidadTrabajador(Integer trabajadorId, Integer servicioId, Date fecha) {
        Usuario trabajador = usuarioRepository.findById(trabajadorId)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
            
        // Solo validar el servicio si se proporciona
        if (servicioId != null) {
            Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.SERVICIO_NO_ENCONTRADO));
                
            if (trabajador.getServicios().stream().noneMatch(s -> s.getId().equals(servicio.getId()))) {
                throw new RuntimeException(ValidationErrorMessages.CITA_TRABAJADOR_NO_SERVICIO);
            }
        }

        // Verificar si el trabajador tiene contrato activo
        if (!contratoRepository.existsByUsuarioIdAndEstadoNombre(trabajador.getId(), "ACTIVO")) {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("mensaje", "Trabajador no disponible - Sin contrato activo");
            responseMap.put("slots", new ArrayList<>());
            return responseMap;
        }
        
        List<Cita> citasDelDia = citaRepository.findByTrabajadorAndFecha(trabajador.getId(), fecha);
        System.out.println("Citas encontradas para el día: " + citasDelDia.size());
        citasDelDia.forEach(c -> System.out.println("Cita existente: " + c.getHoraInicio() + " - " + c.getHoraFin()));
        
        // Obtener el horario del trabajador para ese día
        String diaSemana = new SimpleDateFormat("EEEE", new Locale("es", "ES"))
            .format(fecha).toLowerCase();
            
        List<Map<String, Object>> slots = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        
        trabajador.getHorarios().stream()
            .filter(h -> h.getDiaSemana().name().toLowerCase().equals(diaSemana))
            .forEach(horario -> {
                Calendar calInicio = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
                calInicio.setTime(horario.getHoraInicio());
                
                Calendar calSlot = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
                calSlot.setTime(fecha);
                calSlot.set(Calendar.HOUR_OF_DAY, calInicio.get(Calendar.HOUR_OF_DAY));
                calSlot.set(Calendar.MINUTE, calInicio.get(Calendar.MINUTE));
                calSlot.set(Calendar.SECOND, 0);
                calSlot.set(Calendar.MILLISECOND, 0);
                
                Calendar calFin = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
                calFin.setTime(horario.getHoraFin());
                
                // Generar slots de 30 minutos
                while (calSlot.get(Calendar.HOUR_OF_DAY) < calFin.get(Calendar.HOUR_OF_DAY) || 
                       (calSlot.get(Calendar.HOUR_OF_DAY) == calFin.get(Calendar.HOUR_OF_DAY) && 
                        calSlot.get(Calendar.MINUTE) < calFin.get(Calendar.MINUTE))) {
                    
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("hora", sdf.format(calSlot.getTime()));
                    
                    // Verificar si hay espacio suficiente para el servicio (30 minutos por defecto si no se especifica)
                    Calendar calSlotFin = (Calendar) calSlot.clone();
                    if (servicioId != null) {
                        Servicio servicio = servicioRepository.findById(servicioId).get();
                        calSlotFin.add(Calendar.MINUTE, servicio.getDuracion());
                    } else {
                        calSlotFin.add(Calendar.MINUTE, 30); // Duración por defecto
                    }
                    
                    if (calSlotFin.after(calFin.getTime())) {
                        // No hay suficiente tiempo hasta el fin del horario
                        slot.put("disponible", false);
                        slot.put("motivo", "Fuera de horario");
                    } else {
                        // Verificar si hay citas que se solapan
                        final Date slotInicio = calSlot.getTime();
                        final Date slotFin = calSlotFin.getTime();
                        
                        List<Cita> citasSolapadas = citaRepository.findSolapadas(
                            trabajador.getId(),
                            fecha,
                            slotInicio,
                            slotFin,
                            true
                        );
                        
                        if (!citasSolapadas.isEmpty()) {
                            Cita cita = citasSolapadas.get(0);
                            slot.put("disponible", false);
                            slot.put("ocupadoHasta", sdf.format(cita.getHoraFin()));
                            slot.put("servicio", cita.getServicio().getNombre());
                            slot.put("duracion", (cita.getHoraFin().getTime() - cita.getHoraInicio().getTime()) / (60 * 1000));
                        } else {
                            slot.put("disponible", true);
                        }
                    }
                    
                    slots.add(slot);
                    calSlot.add(Calendar.MINUTE, 30);
                }
            });
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("mensaje", "Disponibilidad recuperada exitosamente");
        responseMap.put("slots", slots);
        return responseMap;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerTrabajadoresDisponiblesParaHora(Integer servicioId, Date fecha, String hora) {
        // Convertir hora string a Date
        String[] partes = hora.split(":");
        Calendar calHora = Calendar.getInstance();
        calHora.setTime(fecha);
        calHora.set(Calendar.HOUR_OF_DAY, Integer.parseInt(partes[0]));
        calHora.set(Calendar.MINUTE, Integer.parseInt(partes[1]));
        calHora.set(Calendar.SECOND, 0);
        calHora.set(Calendar.MILLISECOND, 0);
        
        // Obtener solo trabajadores con contrato activo
        List<Usuario> trabajadoresActivos = usuarioRepository.findAll().stream()
            .filter(t -> t.getRol().equals(RolUsuario.trabajador))
            .filter(t -> contratoRepository.existsByUsuarioIdAndEstadoNombre(t.getId(), "ACTIVO"))
            .toList();
        
        System.out.println("Trabajadores con contrato activo encontrados: " + trabajadoresActivos.size());
        trabajadoresActivos.forEach(t -> System.out.println("Trabajador ID: " + t.getId()));
        
        // Filtrar trabajadores no disponibles
        List<Integer> trabajadoresNoDisponibles = trabajadoresActivos.stream()
            .filter(t -> !esTrabajadorDisponible(t, fecha, calHora.getTime(), null))
            .map(Usuario::getId)
            .toList();
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("mensaje", "Trabajadores no disponibles recuperados exitosamente");
        responseMap.put("trabajadoresNoDisponibles", trabajadoresNoDisponibles);
        return responseMap;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDiasNoDisponiblesParaHora(Integer servicioId, String hora, Date fechaInicio, Date fechaFin) {
        List<Date> diasNoDisponibles = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaInicio);
        
        while (!cal.getTime().after(fechaFin)) {
            String[] partes = hora.split(":");
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(partes[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(partes[1]));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            // Verificar si hay algún trabajador disponible para este servicio en este día y hora
            boolean hayTrabajadorDisponible = usuarioRepository.findByServiciosId(servicioId).stream()
                .anyMatch(t -> esTrabajadorDisponible(t, cal.getTime(), cal.getTime(), servicioId));
                
            if (!hayTrabajadorDisponible) {
                diasNoDisponibles.add(cal.getTime());
            }
            
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // Formatear las fechas como "YYYY-MM-DD"
        List<String> diasFormateados = diasNoDisponibles.stream()
            .map(d -> {
                Calendar temp = Calendar.getInstance();
                temp.setTime(d);
                return String.format("%04d-%02d-%02d",
                    temp.get(Calendar.YEAR),
                    temp.get(Calendar.MONTH) + 1,
                    temp.get(Calendar.DAY_OF_MONTH));
            })
            .toList();
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("mensaje", "Días no disponibles recuperados exitosamente");
        responseMap.put("diasNoDisponibles", diasFormateados);
        return responseMap;
    }
    
    private boolean esTrabajadorDisponible(Usuario trabajador, Date fecha, Date hora, Integer servicioId) {
        // Verificar si tiene contrato activo
        boolean tieneContratoActivo = contratoRepository.existsByUsuarioIdAndEstadoNombre(
            trabajador.getId(), "ACTIVO");
        if (!tieneContratoActivo) {
            System.out.println("Trabajador " + trabajador.getId() + " no tiene contrato activo");
            return false;
        }
        
        // Verificar si tiene horario ese día
        String diaSemana = new java.text.SimpleDateFormat("EEEE", new Locale("es", "ES"))
            .format(fecha).toLowerCase();
            
        boolean tieneHorario = trabajador.getHorarios().stream()
            .anyMatch(h -> h.getDiaSemana().name().toLowerCase().equals(diaSemana));
            
        if (!tieneHorario) {
            System.out.println("Trabajador " + trabajador.getId() + " no tiene horario para " + diaSemana);
            return false;
        }

        // Si se proporciona un servicioId, verificar si el trabajador ofrece ese servicio
        if (servicioId != null && trabajador.getServicios().stream().noneMatch(s -> s.getId().equals(servicioId))) {
            System.out.println("Trabajador " + trabajador.getId() + " no ofrece el servicio " + servicioId);
            return false;
        }

        // Calcular hora de fin (usando 30 minutos como duración por defecto si no se especifica servicio)
        Calendar calInicio = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
        calInicio.setTime(hora);
        calInicio.set(Calendar.SECOND, 0);
        calInicio.set(Calendar.MILLISECOND, 0);

        Calendar calFin = (Calendar) calInicio.clone();
        if (servicioId != null) {
            Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            calFin.add(Calendar.MINUTE, servicio.getDuracion());
        } else {
            calFin.add(Calendar.MINUTE, 30); // Duración por defecto
        }

        // Verificar si hay citas que se solapan con el rango horario
        List<Cita> citasSolapadas = citaRepository.findSolapadas(
            trabajador.getId(),
            fecha,
            calInicio.getTime(),
            calFin.getTime(),
            true
        );

        if (!citasSolapadas.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
            System.out.println("Trabajador " + trabajador.getId() + " tiene citas solapadas:");
            citasSolapadas.forEach(c -> System.out.println("  - " + sdf.format(c.getHoraInicio()) + " - " + sdf.format(c.getHoraFin())));
            return false;
        }

        return true;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerTrabajadoresDisponibles(Integer servicioId) {
        // Validar que el servicio existe
        Servicio servicio = servicioRepository.findById(servicioId)
            .orElseThrow(() -> new RuntimeException(String.format(ValidationErrorMessages.SERVICIO_NO_ENCONTRADO, servicioId)));

        // Obtener trabajadores que ofrecen el servicio y tienen contrato activo
        List<Usuario> trabajadoresDisponibles = usuarioRepository.findByServiciosId(servicioId).stream()
            .filter(t -> t.getRol().equals(RolUsuario.trabajador))
            .filter(t -> contratoRepository.existsByUsuarioIdAndEstadoNombre(t.getId(), "ACTIVO"))
            .toList();

        // Convertir a DTO solo la información necesaria
        List<Map<String, Object>> trabajadoresInfo = trabajadoresDisponibles.stream()
            .map(t -> {
                Map<String, Object> info = new HashMap<>();
                info.put("id", t.getId());
                info.put("nombre", t.getNombre());
                info.put("apellidos", t.getApellidos());
                info.put("foto", t.getFoto());
                return info;
            })
            .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Trabajadores disponibles recuperados exitosamente");
        response.put("trabajadores", trabajadoresInfo);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerTodasLasCitas() {
        List<Cita> citas = citaRepository.findAll();
        List<CitaDTO.CitaRequest> citasDTO = citaConverter.toDtoList(citas);
        CitaDTO response = new CitaDTO();
        response.setCitas(citasDTO);
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("mensaje", "Todas las citas recuperadas exitosamente");
        responseMap.put("citas", response);
        return responseMap;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerCitaPorId(Integer id) {
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.CITA_NO_ENCONTRADA));
        
        // Verificar permisos
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
        
        // Solo el admin, el cliente dueño de la cita o el trabajador asignado pueden verla
        if (!usuario.getRol().equals(RolUsuario.admin) && 
            !cita.getUsuario().getId().equals(usuario.getId()) &&
            !cita.getTrabajador().getId().equals(usuario.getId())) {
            throw new RuntimeException(ValidationErrorMessages.CITA_NO_PERMISOS);
        }
        
        CitaDTO response = new CitaDTO();
        response.setCitas(Collections.singletonList(citaConverter.toDto(cita)));
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("mensaje", "Cita recuperada exitosamente");
        responseMap.put("cita", response);
        return responseMap;
    }

    @Transactional
    public Map<String, Object> reasignarCita(Integer id, CitaDTO.ReasignacionRequest reasignacionRequest) {
        final Cita citaOriginal = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.CITA_NO_ENCONTRADA));
        
        Usuario trabajador = usuarioRepository.findById(reasignacionRequest.getTrabajadorId())
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
        
        // Validar que el trabajador ofrezca el servicio
        if (trabajador.getServicios().stream().noneMatch(s -> s.getId().equals(citaOriginal.getServicio().getId()))) {
            throw new RuntimeException(ValidationErrorMessages.CITA_TRABAJADOR_NO_SERVICIO);
        }
        
        // Crear un CitaRequest temporal para la validación de disponibilidad
        CitaDTO.CitaRequest citaTemp = new CitaDTO.CitaRequest();
        citaTemp.setFecha(reasignacionRequest.getFecha());
        citaTemp.setHoraInicio(reasignacionRequest.getHoraInicio());
        
        // Validar disponibilidad del nuevo trabajador
        validarDisponibilidad(trabajador, citaTemp, citaOriginal.getServicio());
        
        // Actualizar la cita
        citaOriginal.setTrabajador(trabajador);
        citaOriginal.setFecha(reasignacionRequest.getFecha());
        citaOriginal.setHoraInicio(reasignacionRequest.getHoraInicio());
        
        // Calcular nueva hora fin
        Calendar cal = Calendar.getInstance();
        cal.setTime(reasignacionRequest.getHoraInicio());
        cal.add(Calendar.MINUTE, citaOriginal.getServicio().getDuracion());
        citaOriginal.setHoraFin(Time.valueOf(String.format("%02d:%02d:00", 
            cal.get(Calendar.HOUR_OF_DAY), 
            cal.get(Calendar.MINUTE))));
        
        final Cita citaActualizada = citaRepository.save(citaOriginal);
        
        CitaDTO response = new CitaDTO();
        response.setCitas(Collections.singletonList(citaConverter.toDto(citaActualizada)));
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("mensaje", "Cita reasignada exitosamente");
        responseMap.put("cita", response);
        return responseMap;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerCitasPorCliente(Integer clienteId) {
        List<Cita> citas = citaRepository.findByUsuarioId(clienteId);
        List<CitaDTO.CitaRequest> citasDTO = citaConverter.toDtoList(citas);
        CitaDTO response = new CitaDTO();
        response.setCitas(citasDTO);
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("mensaje", "Citas del cliente recuperadas exitosamente");
        responseMap.put("citas", response);
        return responseMap;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerCitasPorTrabajador(Integer trabajadorId) {
        List<Cita> citas = citaRepository.findByTrabajadorId(trabajadorId);
        List<CitaDTO.CitaRequest> citasDTO = citaConverter.toDtoList(citas);
        CitaDTO response = new CitaDTO();
        response.setCitas(citasDTO);
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("mensaje", "Citas del trabajador recuperadas exitosamente");
        responseMap.put("citas", response);
        return responseMap;
    }

    // Nuevos métodos auxiliares
    private void configurarZonaHorariaCitas(CitaDTO citaDTO) {
        for (CitaDTO.CitaRequest citaRequest : citaDTO.getCitas()) {
            if (citaRequest.getHoraInicio() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(citaRequest.getHoraInicio());
                cal.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
                citaRequest.setHoraInicio(Time.valueOf(String.format("%02d:%02d:00", 
                    cal.get(Calendar.HOUR_OF_DAY), 
                    cal.get(Calendar.MINUTE))));
            }
        }
    }

    public Map<String, Object> obtenerTrabajadoresNoDisponiblesConValidacion(
            Integer servicioId, String fechaStr, String hora) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date fecha = dateFormat.parse(fechaStr);
        return obtenerTrabajadoresDisponiblesParaHora(servicioId, fecha, hora);
    }

    public Map<String, Object> obtenerDiasNoDisponiblesConValidacion(
            Integer servicioId, String hora, String fechaInicioStr, String fechaFinStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaInicio = dateFormat.parse(fechaInicioStr);
        Date fechaFin = dateFormat.parse(fechaFinStr);
        return obtenerDiasNoDisponiblesParaHora(servicioId, hora, fechaInicio, fechaFin);
    }
} 