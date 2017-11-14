<?php
    require_once("utilities/conexion.php");

    //fecha es YYY/mm/dd
    class Estadistica extends Conexion{

        const SUCCESS = 1;
        const NO_DATA = 2;
        const UNKNOWN_ERROR = 3;

        public function post($params){
            $opcion = $params[1];
            if($opcion == 'biblioteca'){
                $res = self::getBibliotecas();
                return $res;
            }else if($opcion == 'bibliotecario'){
                $res = self::getBibliotecario();
                return $res;
            }else if($opcion == 'tickets'){
                $res = self::getTickets();
                return $res;
            }         
        }

        private function getBibliotecas(){
            $body = $_POST['info'];
            $info = json_decode($body);
            $fechaInicio = $info->fechaInicio;
            $fechaFin = $info->fechaFin;
            $response = "success";
            $consult = "SELECT plantelAlias, count(*) tickets from ticketCompleto where fechaInicio BETWEEN '$fechaInicio' AND '$fechaFin' GROUP BY plantel";
            $resultado = conexion::queryAsoc($consult);
            //var_dump($resultado);
            if($resultado != null){
                $response = array("status" => self::SUCCESS);
                $response["data"] = $resultado;
            }else{
                $response = array("status" => self::NO_DATA);
            }
               
            return $response;
        }

        private function getBibliotecario(){
            $body = $_POST['info'];
            $info = json_decode($body);
            $fechaInicio = $info->fechaInicio;
            $fechaFin = $info->fechaFin;
            $response = "success";
            $consult = "SELECT u.nombre, count(*) tickets FROM ticket t inner join usuarios u on t.asignadoA = u.id WHERE fechaInicio BETWEEN '$fechaInicio' AND '$fechaFin' group by t.asignadoA";
            $resultado = conexion::queryAsoc($consult);
            //var_dump($resultado);
            if($resultado != null){
                $response = array("status" => self::SUCCESS);
                $response["data"] = $resultado;
            }else{
                $response = array("status" => self::NO_DATA);
            }
               
            return $response;
        }

        private function getTickets(){
            $body = $_POST['info'];
            $info = json_decode($body);
            $fechaInicio = $info->fechaInicio;
            $fechaFin = $info->fechaFin;
            $response = "success";
            $consult= "(SELECT 'cerrado' estatus, count(*) cantidad FROM `ticket` WHERE estado = 10 and fechaInicio BETWEEN '$fechaInicio' AND '$fechaFin')
            UNION
            (SELECT 'nuevo', count(*) nuevos FROM `ticket` WHERE estado = 1 and fechaInicio BETWEEN '$fechaInicio' AND '$fechaFin')
            UNION
            (SELECT 'proceso', count(*) proceso FROM `ticket` WHERE estado <> 1 and estado <> 10 and fechaInicio BETWEEN '$fechaInicio' AND '$fechaFin')";

            $resultado = conexion::queryAsoc($consult);
            //var_dump($resultado);
            if($resultado != null){
                $response = array("status" => self::SUCCESS);
                $response["data"] = $resultado;
            }else{
                $response = array("status" => self::NO_DATA);
            }
               
            return $response;
        }
    }
?>