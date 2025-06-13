-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: peluqueria_db
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `id` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT INTO `categoria` VALUES (1,'Categoría por defecto para productos no clasificados','Otros productos'),(7,'Productos relacionados con los tintes de pelo','Tintes para el cabello'),(8,'Productos relacionados con los fijadores de pelo','Fijadores de Pelo'),(60,'Distintos estilos y formatos de secadores','Secadores');
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cita`
--

DROP TABLE IF EXISTS `cita`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cita` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `hora_fin` time(6) NOT NULL,
  `hora_inicio` time(6) NOT NULL,
  `id_estado` int NOT NULL,
  `id_servicio` int NOT NULL,
  `id_trabajador` int NOT NULL,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6dirrhf28fq8ijbkkgp84tu5v` (`id_estado`),
  KEY `FK68785k2hlh38mhiq3u2ny82p4` (`id_servicio`),
  KEY `FK2wqmixrq50o2hn3of0qkaitat` (`id_trabajador`),
  KEY `FK35jgeru8qufaq4p8akigahlic` (`id_usuario`),
  CONSTRAINT `FK2wqmixrq50o2hn3of0qkaitat` FOREIGN KEY (`id_trabajador`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FK35jgeru8qufaq4p8akigahlic` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FK68785k2hlh38mhiq3u2ny82p4` FOREIGN KEY (`id_servicio`) REFERENCES `servicio` (`id`),
  CONSTRAINT `FK6dirrhf28fq8ijbkkgp84tu5v` FOREIGN KEY (`id_estado`) REFERENCES `estado` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cita`
--

LOCK TABLES `cita` WRITE;
/*!40000 ALTER TABLE `cita` DISABLE KEYS */;
INSERT INTO `cita` VALUES (12,'2025-06-26','19:30:00.000000','19:00:00.000000',7,7,71,2),(13,'2025-06-26','19:00:00.000000','18:30:00.000000',8,7,71,2),(20,'2025-06-17','10:00:00.000000','09:30:00.000000',6,7,51,2),(21,'2025-06-16','10:30:00.000000','10:00:00.000000',6,10,72,2);
/*!40000 ALTER TABLE `cita` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `citas`
--

DROP TABLE IF EXISTS `citas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `citas` (
  `id` int NOT NULL AUTO_INCREMENT,
  `estado` varchar(255) NOT NULL,
  `fecha` date NOT NULL,
  `hora` time(6) NOT NULL,
  `servicio` int NOT NULL,
  `trabajador` int NOT NULL,
  `usuario_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdg1071btxrno7orihyb4l5llj` (`usuario_id`),
  CONSTRAINT `FKdg1071btxrno7orihyb4l5llj` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `citas`
--

LOCK TABLES `citas` WRITE;
/*!40000 ALTER TABLE `citas` DISABLE KEYS */;
/*!40000 ALTER TABLE `citas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contrato`
--

DROP TABLE IF EXISTS `contrato`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contrato` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha_fin_contrato` date DEFAULT NULL,
  `fecha_inicio_contrato` date NOT NULL,
  `tipo_contrato` enum('fijo','temporal') NOT NULL,
  `url_contrato` varchar(255) DEFAULT NULL,
  `estado_id` int NOT NULL,
  `usuario_id` int NOT NULL,
  `salario` decimal(38,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh8oj01lhtkv1kj548h4emwt0h` (`estado_id`),
  KEY `FKjk83wy5pq0a7hufodligiop2k` (`usuario_id`),
  CONSTRAINT `FKh8oj01lhtkv1kj548h4emwt0h` FOREIGN KEY (`estado_id`) REFERENCES `estado` (`id`),
  CONSTRAINT `FKjk83wy5pq0a7hufodligiop2k` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contrato`
--

LOCK TABLES `contrato` WRITE;
/*!40000 ALTER TABLE `contrato` DISABLE KEYS */;
INSERT INTO `contrato` VALUES (9,NULL,'2025-05-09','fijo','/contratos/contrato_51_1746785893322.pdf',9,51,15000.00),(16,'2025-05-11','2025-05-10','temporal','/contratos/contrato_67_1746805576847.pdf',11,67,5000.00),(20,NULL,'2025-05-16','fijo','/contratos/contrato_71_1747381722196.pdf',9,71,15000.00),(21,NULL,'2025-05-16','fijo','/contratos/contrato_72_1747381835569.pdf',9,72,18000.00),(22,'2026-06-05','2025-06-05','temporal','/contratos/contrato_73_1749118630834.pdf',9,73,150000.00),(23,NULL,'2025-06-05','fijo','/contratos/contrato_67_1749118720155.pdf',9,67,140000.00),(37,NULL,'2025-06-11','fijo','/contratos/contrato_92_1749664183315.pdf',9,92,15000.00);
/*!40000 ALTER TABLE `contrato` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contratos`
--

DROP TABLE IF EXISTS `contratos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contratos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `documento_pdf_url` varchar(255) DEFAULT NULL,
  `fecha_fin_contrato` date DEFAULT NULL,
  `fecha_inicio_contrato` date NOT NULL,
  `tipo_contrato` enum('fijo','temporal') NOT NULL,
  `url_contrato` varchar(255) DEFAULT NULL,
  `estado_id` int NOT NULL,
  `usuario_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5695xprathr72jm2vln7ehned` (`estado_id`),
  KEY `FKo6ktbaxnx5cob50lak5elv2tq` (`usuario_id`),
  CONSTRAINT `FK5695xprathr72jm2vln7ehned` FOREIGN KEY (`estado_id`) REFERENCES `estado` (`id`),
  CONSTRAINT `FKo6ktbaxnx5cob50lak5elv2tq` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contratos`
--

LOCK TABLES `contratos` WRITE;
/*!40000 ALTER TABLE `contratos` DISABLE KEYS */;
/*!40000 ALTER TABLE `contratos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado`
--

DROP TABLE IF EXISTS `estado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `tipo_estado` enum('CITA','CONTRATO','PEDIDO') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9k9kjgxlp4kpe5ggc3otdp8fy` (`nombre`,`tipo_estado`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado`
--

LOCK TABLES `estado` WRITE;
/*!40000 ALTER TABLE `estado` DISABLE KEYS */;
INSERT INTO `estado` VALUES (2,'ACEPTADO','PEDIDO'),(9,'ACTIVO','CONTRATO'),(8,'CANCELADA','CITA'),(5,'CANCELADO','PEDIDO'),(7,'COMPLETADA','CITA'),(4,'COMPLETADO','PEDIDO'),(3,'ENVIADO','PEDIDO'),(11,'INACTIVO','CONTRATO'),(10,'PENDIENTE','CONTRATO'),(1,'PENDIENTE','PEDIDO'),(6,'PROGRAMADA','CITA');
/*!40000 ALTER TABLE `estado` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `horario`
--

DROP TABLE IF EXISTS `horario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `horario` (
  `id` int NOT NULL AUTO_INCREMENT,
  `dia_semana` enum('domingo','jueves','lunes','martes','miércoles','sábado','viernes') DEFAULT NULL,
  `hora_fin` time(6) DEFAULT NULL,
  `hora_inicio` time(6) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `horario`
--

LOCK TABLES `horario` WRITE;
/*!40000 ALTER TABLE `horario` DISABLE KEYS */;
INSERT INTO `horario` VALUES (1,'lunes','14:00:00.000000','08:00:00.000000','Lunes Turno de mañana'),(5,'martes','14:00:00.000000','08:00:00.000000','Martes Turno de mañana'),(6,'jueves','19:30:00.000000','17:00:00.000000','Jueves Turno de Tarde'),(11,'viernes','14:00:00.000000','09:00:00.000000','Viernes Turno de mañana');
/*!40000 ALTER TABLE `horario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `horario_trabajador`
--

DROP TABLE IF EXISTS `horario_trabajador`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `horario_trabajador` (
  `trabajador_id` int NOT NULL,
  `horario_id` int NOT NULL,
  KEY `FK549jjnxhiu81kpw4ciqj197jh` (`horario_id`),
  KEY `FKiy76mmta9lh12a9nvtyneh9e4` (`trabajador_id`),
  CONSTRAINT `FK549jjnxhiu81kpw4ciqj197jh` FOREIGN KEY (`horario_id`) REFERENCES `horario` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKiy76mmta9lh12a9nvtyneh9e4` FOREIGN KEY (`trabajador_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `horario_trabajador`
--

LOCK TABLES `horario_trabajador` WRITE;
/*!40000 ALTER TABLE `horario_trabajador` DISABLE KEYS */;
INSERT INTO `horario_trabajador` VALUES (51,5),(67,1),(67,5),(71,5),(71,6),(73,1),(73,5),(72,1),(72,6),(92,11);
/*!40000 ALTER TABLE `horario_trabajador` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedido`
--

DROP TABLE IF EXISTS `pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedido` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha_pedido` datetime(6) NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `estado_id` int NOT NULL,
  `usuario_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlpuc2kd4q97wd68te94hcw8sl` (`estado_id`),
  KEY `FK6uxomgomm93vg965o8brugt00` (`usuario_id`),
  CONSTRAINT `FK6uxomgomm93vg965o8brugt00` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKlpuc2kd4q97wd68te94hcw8sl` FOREIGN KEY (`estado_id`) REFERENCES `estado` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedido`
--

LOCK TABLES `pedido` WRITE;
/*!40000 ALTER TABLE `pedido` DISABLE KEYS */;
INSERT INTO `pedido` VALUES (1,'2025-05-22 21:38:31.132040',67.98,1,2),(2,'2025-06-05 11:34:32.016466',23.99,2,2),(4,'2025-06-10 21:30:27.166053',67.98,2,2),(5,'2025-06-10 21:31:35.484458',12.50,1,2),(6,'2025-06-10 21:31:47.491126',46.49,1,2);
/*!40000 ALTER TABLE `pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedido_producto`
--

DROP TABLE IF EXISTS `pedido_producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedido_producto` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `nombre_producto` varchar(255) DEFAULT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `pedido_id` int NOT NULL,
  `producto_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7uyg0ynfe4wadl7ha9bmtynvm` (`pedido_id`),
  KEY `FKl9lfd6a7bi0o5qn2f3epfbpin` (`producto_id`),
  CONSTRAINT `FK7uyg0ynfe4wadl7ha9bmtynvm` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `FKl9lfd6a7bi0o5qn2f3epfbpin` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedido_producto`
--

LOCK TABLES `pedido_producto` WRITE;
/*!40000 ALTER TABLE `pedido_producto` DISABLE KEYS */;
INSERT INTO `pedido_producto` VALUES (1,2,'Eva divina color studio',33.99,1,2),(2,1,'Tricomix Gel Hair',23.99,2,1),(4,2,'Eva divina color studio',33.99,4,2),(5,1,'Gel fijador coco Eseuve',12.50,5,3),(6,1,'Eva divina color studio',33.99,6,2),(7,1,'Gel fijador coco Eseuve',12.50,6,3);
/*!40000 ALTER TABLE `pedido_producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto` (
  `id` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) NOT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `foto` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `precio` decimal(10,2) NOT NULL,
  `stock` int NOT NULL,
  `id_categoria` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9nyueixdsgbycfhf7allg8su` (`id_categoria`),
  CONSTRAINT `FK9nyueixdsgbycfhf7allg8su` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto`
--

LOCK TABLES `producto` WRITE;
/*!40000 ALTER TABLE `producto` DISABLE KEYS */;
INSERT INTO `producto` VALUES (1,'Fijador potente con eucalipto','2025-04-29 19:01:07.554000','/uploads/productos/65af3898-3de8-4edd-bf90-5dc779a9b2f0_fijadorpelo1.png','Tricomix Gel Hair',23.99,60,8),(2,'Mascarilla de color fucsia','2025-04-29 19:02:33.181000','/uploads/productos/b9d13ebd-47b9-42bf-99a6-2269c56a906b_tintepelo1.png','Eva divina color studio',33.99,49,7),(3,'Fijador para el pelo con extracto de coco','2025-05-05 18:03:51.672000','/uploads/productos/34a30289-a63b-4a14-b5d3-d5eb71e79296_fijadorpelo2.png','Gel fijador coco Eseuve',12.50,58,8),(44,'Potencia y cuidado capilar con tecnología iónica, motor silencioso.','2025-06-11 19:20:50.912000','/uploads/productos/fa525822-a7d7-4ec2-8d65-99739d1b4098_secador1.png','Deluxe Ionic 2300W AC',52.90,65,60),(45,'Máquina de cortar pelo profesional','2025-06-11 19:23:41.201000','/uploads/productos/3e402b37-bfc2-44a2-84b1-7940ccb4d4b1_maquinilla1.png','Máquina Moser Max 45',120.00,32,1);
/*!40000 ALTER TABLE `producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `servicio`
--

DROP TABLE IF EXISTS `servicio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `servicio` (
  `id` int NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) NOT NULL,
  `duracion` int NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `precio` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `servicio`
--

LOCK TABLES `servicio` WRITE;
/*!40000 ALTER TABLE `servicio` DISABLE KEYS */;
INSERT INTO `servicio` VALUES (7,'El corte tradicional que nunca pasa de moda',30,'Corte clásico',13.00),(8,'Refresca tu melena con un estilo impecable',30,'Lavado y peinado',15.00),(9,'Color vibrante y duradero para un cambio radical',60,'Coloración',17.00),(10,'Embellece tus uñas en tiempo récord',30,'Manicura',15.00),(11,'Afeitado y arreglo de barba para un look impecable',30,'Barbería completa',10.00),(12,'Revitaliza y fortalece tu cabello',30,'Tratamiento capilar',11.00);
/*!40000 ALTER TABLE `servicio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` int NOT NULL AUTO_INCREMENT,
  `apellidos` varchar(255) NOT NULL,
  `carrito` longtext,
  `direccion` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `fecha_registro` datetime(6) DEFAULT NULL,
  `foto` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `contraseña` varchar(255) NOT NULL,
  `rol` enum('admin','cliente','trabajador') NOT NULL,
  `telefono` varchar(15) NOT NULL,
  `salario` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5171l57faosmj8myawaucatdw` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'administrador',NULL,'Infanta Margarita','admin@gmail.com','2025-04-26 13:02:50.268000','/uploads/users/fotos/6c2e604d-41a9-41bf-8321-36ea36ed3118_admin.png','Admin','$2a$10$hGkDSP2vAdCtBTP9/5ix1u09j6j3IDvhiVmtjc5FxShCfIgq7Qlxe','admin','693209523',NULL),(2,'García Castillo','[{\"precioUnitario\":52.9,\"productoId\":44,\"cantidad\":1,\"nombreProducto\":\"Deluxe Ionic 2300W AC\"}]','Infanta Pilar 21, Las Infantas','martin@gmail.com','2025-04-26 14:58:05.523000','/uploads/users/fotos/ce9d5caf-0007-4cc1-a0a0-580ada36f8ab_IMG-20191222-WA0022.jpg','Martín','$2a$10$uYtNEc7NM1m6WhSS7aGzPuwsVylH.9nYJW8P.Y26D8hrnluhUESh6','cliente','693209523',NULL),(51,'Fernández',NULL,'Calle Navas de Tolosa 12, 1ºA, Jaén','lucas@gmail.com','2025-05-09 12:18:13.319000','/uploads/users/fotos/a010e719-58b5-4227-969b-f83021075b42_peluquero1.png','Lucas','$2a$10$CMkow6u5e1B645MqZ3RiA.IkuSipiMjSRrtL5RQE/sP35W4M/c5A2','trabajador','612345678',NULL),(67,'Ramírez',NULL,'Avenida de Andalucía 45, Jaén','diego@gmail.com','2025-05-09 17:46:16.844000','/uploads/users/fotos/656d4f60-dfa0-4ebf-b2af-e1f99de29b8e_peluquero3.png','Diego','$2a$10$R4znXRk1XCIKDrwPtc4AFu4d0FXCoCCpKTYXAkwAy2GND5ZrvVMFi','trabajador','623987456',NULL),(71,'Morales',NULL,'Calle Millán de Priego 7, Jaén','javirod@gmail.com','2025-05-16 09:48:42.192000','/uploads/users/fotos/58ca269a-1844-4189-a566-26578b5ddf0d_peluquero2.png','Javier','$2a$10$tP.nnWr.G3DAQDRBDDQ2Ku5e3f8PiZuU0Zpl8JJJEIRcZM2/Z3Xxy','trabajador','634228119',NULL),(72,'Gómez',NULL,'Calle Federico Mendizábal 9, Jaén','tamara@gmail.com','2025-05-16 09:50:35.565000','/uploads/users/fotos/b8234d94-3579-4b60-a429-4a67c9387ec9_peluquero4.png','Tamara','$2a$10$f22wlhW9yq7xEYtu2PdfcuZWWpVrpepW5KaKIjEIBqbHLZtjgzrSW','trabajador','656882134',NULL),(73,'López',NULL,'Calle Arquitecto Berges 33, Jaén','adrian@gmail.com','2025-06-05 12:17:10.827000','/uploads/users/fotos/5e78b789-4519-4003-b310-9a91c14e9f8f_peluquero6.png','Adrián','$2a$10$YJ3qNGODV8wszwQJp/wMsuZKHBjzH6t/EYN8Rhq6CscTwjbf4y8y.','trabajador','645779300',NULL),(92,'Torres',NULL,'Paseo de la Estación 56, Jaén','marina@gmail.com','2025-06-11 19:49:43.311000','/uploads/users/fotos/deaedfad-b70c-46ef-904a-2710eefae346_peluquero5.png','Marina','$2a$10$GkiX9vvxQjL247kP/SAtF.SnvpT2WmJ2FVfhs6Om1OVTDantKkeaG','trabajador','667543209',NULL);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_has_servicio`
--

DROP TABLE IF EXISTS `usuario_has_servicio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_has_servicio` (
  `usuario_id` int NOT NULL,
  `servicio_id` int NOT NULL,
  KEY `FKbvdkn25tl3id6b9y2ihsfxgl6` (`usuario_id`),
  KEY `FK24xpbew580fa4wk6qxqw05n8i` (`servicio_id`),
  CONSTRAINT `FK24xpbew580fa4wk6qxqw05n8i` FOREIGN KEY (`servicio_id`) REFERENCES `servicio` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKbvdkn25tl3id6b9y2ihsfxgl6` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_has_servicio`
--

LOCK TABLES `usuario_has_servicio` WRITE;
/*!40000 ALTER TABLE `usuario_has_servicio` DISABLE KEYS */;
INSERT INTO `usuario_has_servicio` VALUES (51,7),(51,8),(51,11),(51,12),(67,7),(71,7),(71,11),(73,8),(73,11),(72,8),(72,10),(72,12),(92,7),(92,8),(92,9),(92,10);
/*!40000 ALTER TABLE `usuario_has_servicio` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-13  2:44:41
