<?php
    require_once("utilities/conexion.php");

    class User extends Conexion{
        const SUCCESS = 1;
        const ACCESS_DENIED = 2;
        const UNKNOWN_ERROR = 3;

        public function post($info){
            $opcion = $info[1];
            //var_dump($opcion);
            if($opcion == 'login'){
                $res = self::login();
            }else if($opcion == 'register'){
                $res = self::registrar();
                return $res;
            }            
            return $res;
        }

        public function get($params){
            $consult = "SELECT id, etiqueta FROM plantel WHERE tipo = 1";
            $response = "success";
            $resultado = conexion::queryAsoc($consult);
            
            if($resultado == null){
                $response = array("status" => self::UNKNOWN_ERROR);
            }else{
                $response = array("status" => self::SUCCESS);
                $response["datos"] = $resultado;
            }
            return $response;
        }

        private function login(){
            $body = $_POST['info'];
            $info = json_decode($body);
            if(self::validateStructure($info)){
                return self::auth($info);
            }else{
                $response = array("status" => self::ACCESS_DENIED );
                return $response;
            }
        }

        private function auth($info){
            $email = $info->email;
            $password = $info->password;
            $response = "success";
            $consult = "SELECT COUNT(id) count, id, dependencia, tipoUsuario FROM usuarios WHERE (correo = '$email' AND password = SHA1('$password'))";
        
            $resultado = conexion::query_single_object($consult);
            
            if($resultado == null){
                $response = array("status" => self::UNKNOWN_ERROR);
            }else if($resultado->count == 0){
                $response = array("status" => self::ACCESS_DENIED);
            }else{
                $response = array("status" => self::SUCCESS);
                $response["idUsuario"] = $resultado->id;
                $response["dependencia"] = $resultado->dependencia;
                $response["tipoUsuario"] = $resultado->tipoUsuario;
            }
            return $response;
        }


        private function registrar(){
            $body = $_POST['info'];
            $info = json_decode($body);
            if(self::validateStructureRegister($info)){
                return self::insert($info);
            }else{
                $response = array("status" => self::NOT_REGISTERED);
                return $response;
            }
        }

        public function insert($info){
            $password = $info->password;
            $correo = $info->email;
            $nombre = $info->nombre;
            $dependencia = $info->dependencia;
            $tipoUsuario = $info->tipoUsuario;
            $consultVerificar = "SELECT COUNT(id) count FROM usuarios WHERE (correo = '$correo' AND password = '$password')";
            $consultInsert = "INSERT INTO usuarios VALUES(null,'$nombre',SHA1('$password'),'$correo','$dependencia',
            $tipoUsuario)";
            $resultado = conexion::query_single_object($consultVerificar);
            if($resultado == null){
                $response = array("status" => self::UNKNOWN_ERROR);
            }else if($resultado->count == 0){
                $retorno = conexion::query($consultInsert);
                if($retorno){
                    $response = array("status" => self::SUCCESS);
                }else{
                    $response = array("status" => self::ACCESS_DENIED);
                }
            }else{
                $response = array("status" => self::ACCESS_DENIED);
            }
            return $response;
        }

        private function validateStructureRegister($info){
			$response = FALSE;
			if ($info !== NULL && count($info) === 1 && isset($info->password) && isset($info->email)
            && isset($info->nombre) && isset($info->dependencia) && isset($info->tipoUsuario)){
                $response = TRUE;
            }
	    	return $response;
		}

        private function validateStructure($info){
			$response = FALSE;
			if ($info !== NULL && count($info) === 1 && isset($info->password)){
                $response = TRUE;
            }
	    	return $response;
        }
    }
?>