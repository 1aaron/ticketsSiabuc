<?php
    require_once("utilities/conexion.php");

    //fecha es YYY/mm/dd
    class Usuario extends Conexion{

        const SUCCESS = 1;
        const NO_DATA = 2;
        const UNKNOWN_ERROR = 3;

        public function post($params){
            $opcion = $params[1];
            if($opcion == 'plantel'){
                $res = self::getUsers();
                return $res;
            }else if($opcion == 'asignar'){
                $res = self::assign();
                return $res;
            }else if($opcion == 'transferir'){
                $res = self::transfer();
                return $res;
            }else if($opcion == 'encargado'){
                $res = self::getEncargados();
                return $res;
            }else if($opcion == 'administrar'){
                $res = self::getAdministrables();
                return $res;
            }else if($opcion == 'rol'){
                $res = self::asignarRol();
                return $res;
            }           
        }

        private function asignarRol(){
            $body = $_POST['info'];
            $info = json_decode($body);
            $rol = $info->rol;
            $idUsuario = $info->idUsuario;
            $response = "success";
            $consult = "UPDATE usuarios SET tipoUsuario = $rol WHERE id = $idUsuario";
            $resultado = conexion::query($consult);
            if($resultado == true){
                $response = array("status" => self::SUCCESS);
            }else{
                $response = array("status" => self::NO_DATA);
            }
               
            return $response;
        }

        private function getAdministrables(){
            $body = $_POST['info'];
            $info = json_decode($body);
            $dependencia = $info->dependencia;
            $tipoUsuario = $info->tipoUsuario;
            $response = "success";
            $consult = "";
            if($tipoUsuario == 2){
                $consult = "SELECT u.id, nombre, correo, dependencia, tipoUsuario, p.etiqueta FROM usuarios u LEFT JOIN plantel p
                ON u.dependencia = p.id WHERE dependencia = $dependencia";
            }else{
                $consult = "SELECT u.id, nombre, correo, dependencia, tipoUsuario, p.etiqueta FROM usuarios u LEFT JOIN plantel p
                ON u.dependencia = p.id";
            }
            $resultado = conexion::queryAsoc($consult);       
            if($resultado == null){
                $response = array("status" => self::NO_DATA);
            }else{
                $response = array("status" => self::SUCCESS);
                $response["datos"] = $resultado;
            }
            return $response;
        }

        private function getEncargados(){
            $body = $_POST['info'];
            $info = json_decode($body);
            $dependencia = $info->dependencia;
            $idUsuario = $info->idUsuario;
            $response = "success";
            $consult = "SELECT id, nombre FROM usuarios WHERE id != $idUsuario AND tipoUsuario = 2";
            $resultado = conexion::queryAsoc($consult);
            if($resultado == null){
                $response = array("status" => self::NO_DATA);
            }else{
                $response = array("status" => self::SUCCESS);
                $response["datos"] = $resultado;
            }
            return $response;
        }

        private function getUsers(){
            $body = $_POST['info'];
            $info = json_decode($body);
            $dependencia = $info->dependencia;
            $response = "success";
            $consult = "SELECT id, nombre FROM usuarios WHERE dependencia = $dependencia AND tipoUsuario = 1";
            $resultado = conexion::queryAsoc($consult);
            if($resultado == null){
                $response = array("status" => self::NO_DATA);
            }else{
                $response = array("status" => self::SUCCESS);
                $response["datos"] = $resultado;
            }
            return $response;
        }

        private function assign(){
            $body = $_POST['info'];
            $info = json_decode($body);
            $response = "success";
            
            $folio = $info->folio;
            $estado = $info->estado;
            $usuario = $info->usuario;
            $asignacion = $info->asignacion;
            $fecha = $info->fecha;
            $consult = "UPDATE ticket SET asignadoA = $asignacion, estado = $estado WHERE folio = $folio";

            $resultado = conexion::query($consult);
            if($resultado == true){
                $consult = "INSERT INTO historial VALUES($folio,'$fecha',$estado,$usuario,'','')";
                conexion::query($consult);
                $response = array("status" => self::SUCCESS);
            }else{
                $response = array("status" => self::NO_DATA);
            }
                    
        return $response;
        }

        private function transfer(){
            $body = $_POST['info'];
            $info = json_decode($body);
            $response = "success";
            
            $folio = $info->folio;
            $estado = $info->estado;
            $usuario = $info->usuario;
            $asignacion = $info->asignacion;
            $fecha = $info->fecha;
            $consult = "UPDATE ticket SET asignadoA = $asignacion, estado = 7 WHERE folio = $folio";

            $resultado = conexion::query($consult);
            if($resultado == true){
                $consult = "INSERT INTO historial VALUES($folio,'$fecha',7,$usuario,'','')";
                conexion::query($consult);
                $response = array("status" => self::SUCCESS);
            }else{
                $response = array("status" => self::NO_DATA);
            }
                    
        return $response;
        }

        private function validateStructure($info){
			$response = FALSE;
            if ($info !== NULL && count($info) === 1 && isset($info->folio) && isset($info->estado)
            && isset($info->usuario) && isset($info->fecha) && isset($info->respuesta) && isset($info->comentario)){
                $response = TRUE;
            }
	    	return $response;
		}
    }
?>