<?php
    require_once("utilities/conexion.php");

    //fecha es YYY/mm/dd
    class Historial extends Conexion{

        const SUCCESS = 1;
        const NO_DATA = 2;
        const UNKNOWN_ERROR = 3;

        public function post($params){
            $opcion = $params[1];
            if($opcion == 'obtener'){
                $res = self::getHistorial();
                return $res;
            }else if($opcion == 'insertar'){
                $res = self::insert();
                return $res;
            }          
        }

        private function getHistorial(){
            $body = $_POST['info'];
            $info = json_decode($body);
            $folio = $info->folio;
            $response = "success";
            $consult = "SELECT * FROM historialCompleto WHERE folioTicket = $folio ORDER BY fecha DESC";
            $resultado = conexion::queryAsoc($consult);
            if($resultado == null){
                $response = array("status" => self::NO_DATA);
            }else{
                $response = array("status" => self::SUCCESS);
                $response["datos"] = $resultado;
            }
            return $response;
        }

        private function insert(){
            $body = $_POST['info'];
            $info = json_decode($body);
            $response = "success";
            if(self::validateStructure($info)){
                $folio = $info->folio;
                $estado = $info->estado;
                $usuario = $info->usuario;
                $respuesta = $info->respuesta;
                $comentario = $info->comentario;
                $fecha = $info->fecha;
                $consult = "INSERT INTO historial VALUES($folio,'$fecha',$estado,$usuario,'$respuesta','$comentario')";
                $resultado = conexion::query($consult);
                if($resultado == true){
                    $consult = "UPDATE ticket SET estado = $estado WHERE folio = $folio";
                    conexion::query($consult);
                    $response = array("status" => self::SUCCESS);
                }else{
                    $response = array("status" => self::NO_DATA);
                }
            }else{
                $response = array('status' =>self::UNKNOWN_ERROR);
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