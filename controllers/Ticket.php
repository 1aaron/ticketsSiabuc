<?php
    require_once("utilities/conexion.php");

    //fecha es YYY/mm/dd
    class Ticket extends Conexion{

        const SUCCESS = 1;
        const ACCESS_DENIED = 2;
        const UNKNOWN_ERROR = 3;

        public function post($params){
            $opcion = $params[1];
            if($opcion == 'insert'){
                $res = self::insert();
                return $res;
            }else if($opcion == 'misTickets'){
                $res = self::getMios();
                return $res;
            }else if($opcion == 'libres'){
                $body = $_POST['info'];
                $info = json_decode($body);
                $res = self::getLibres($info);
                return $res;
            }else if($opcion == 'cerrar'){
                $body = $_POST['info'];
                $info = json_decode($body);
                $res = self::cerrar($info);
                return $res;
            }else if($opcion == 'calificar'){
                $body = $_POST['info'];
                $info = json_decode($body);
                $res = self::calificar($info);
                return $res;
            }else if($opcion == 'tomar'){
                $body = $_POST['info'];
                $info = json_decode($body);
                $res = self::tomar($info);
                return $res;
            }            
        }

        public function tomar($info){
            $folio = $info->folio;
            $fecha = $info->fecha;
            $usuario = $info->usuario;
            $response = "success";
            $consult = "UPDATE ticket SET estado = 3, asignadoA = $usuario WHERE folio = $folio";
            $resultado = conexion::query($consult);
            if($resultado == true){
                $consult = "INSERT INTO historial VALUES($folio,'$fecha',3,$usuario,'','')";
                conexion::query($consult);
                $response = array("status" => self::SUCCESS);
            }else{
                $response = array("status" => self::ACCESS_DENIED);
            }
            return $response;
        }

        public function calificar($info){
            $calificacion = $info->calificacion;
            $folio = $info->folio;
            $consult = "UPDATE ticket SET calificacion = $calificacion WHERE folio = $folio";
            $res = conexion::query($consult);
            if($res == true){
                $response = array("status" => self::SUCCESS);
            }else{
                $response = array("status" => self::ACCESS_DENIED);
            }
            return $response;
        }

        public function cerrar($info){
            $folio = $info->folio;
            $fecha = $info->fecha;
            $usuario = $info->usuario;
            $respuesta = $info->respuesta;
            $comentario = $info->comentario;
            $response = "success";
            $consult = "INSERT INTO historial VALUES($folio,'$fecha',10,$usuario,'$respuesta','$comentario')";
            $resultado = conexion::query($consult);
            if($resultado == true){
                $consult = "UPDATE ticket SET estado = 10, fechaCierre = '$fecha' WHERE folio = $folio";
                conexion::query($consult);
                $response = array("status" => self::SUCCESS);
            }else{
                $response = array("status" => self::ACCESS_DENIED);
            }
            return $response;
        }

        public function getLibres($info){
            $dependencia = $info->dependencia;
            $tipoUsuario = $info->tipoUsuario;
            if($tipoUsuario == 3){
                $consult = "SELECT * FROM ticketsLibres";
                $response = "success";
                $resultado = conexion::queryAsoc($consult);
                
                if($resultado == null){
                    $response = array("status" => self::ACCESS_DENIED);
                }else{
                    $response = array("status" => self::SUCCESS);
                    $response["datos"] = $resultado;
                }
                return $response;
            }else{
                $consult = "SELECT * FROM ticketsLibres WHERE dependencia = $dependencia";
                $response = "success";
                $resultado = conexion::queryAsoc($consult);
                
                if($resultado == null){
                    $response = array("status" => self::ACCESS_DENIED);
                }else{
                    $response = array("status" => self::SUCCESS);
                    $response["datos"] = $resultado;
                }
                return $response;
            }
        }

        private function getMios(){
            $body = $_POST['info'];
            $info = json_decode($body);
            return self::obtenerMios($info);
        }

        private function insert(){
            $body = $_POST['info'];
            $info = json_decode($body);
            if(self::validateStructure($info)){
                return self::guardar($info);
            }else{
                $response = array("status" => self::ACCESS_DENIED );
                return $response;
            }
        }

        private function obtenerMios($info){
            $tipoUsuario = $info->tipoUsuario;
            $idUsuario = $info->idUsuario;
            $plantel = $info->plantel;
            $response = "success";
            if($tipoUsuario == 0){
                $consult = "SELECT * FROM ticketCompleto WHERE usuario = $idUsuario ORDER BY fechaInicio DESC";
            }else if($tipoUsuario == 1){
                $consult = "SELECT * FROM ticketCompleto WHERE asignadoA = $idUsuario ORDER BY fechaInicio DESC";
            }else if($tipoUsuario==2){
                $consult = "SELECT * FROM ticketCompleto WHERE plantel = $plantel OR asignadoA = $idUsuario ORDER BY fechaInicio DESC";                
            }else{
                $consult = "SELECT * FROM ticketCompleto";
            }
            $resultado = conexion::queryAsoc($consult);
            if($resultado == null){
                $response = array("status" => self::ACCESS_DENIED);
            }else{
                $response = array("status" => self::SUCCESS);
                $response["datos"] = $resultado;
            }
            return $response;
        }

        private function guardar($info){
            $usuario = $info->usuario;
            $peticion = $info->peticion;
            $fecha = $info->fecha;

            $response = "success";
            $consult = "INSERT INTO ticket(usuario,peticion,fechaInicio,estado, calificacion) VALUES ({$usuario},'{$peticion}','{$fecha}',1,1)";

            $resultado = conexion::query($consult);
            
            if($resultado == null){
                $response = array("status" => self::ACCESS_DENIED);
            }else if($resultado == true){
                $response = array("status" => self::SUCCESS);
            }
            return $response;
        }

        private function validateStructure($info){
			$response = FALSE;
            if ($info !== NULL && count($info) === 1 && isset($info->usuario) && isset($info->peticion)
            && isset($info->fecha)){
                $response = TRUE;
            }
	    	return $response;
		}
    }
?>