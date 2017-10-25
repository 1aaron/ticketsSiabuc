<?php
    require_once("mysql.php");

    class Conexion{
        private static $dbcon = null;
        private static $db = null;

        final private function __construct(){
            self::conect();
        }
        public static function conect(){
            self::$dbcon = new mysqli(NOMBRE_HOST, USUARIO, CONTRASENA, BASE_DE_DATOS);
            mysqli_set_charset(self::$dbcon,"utf8");
            if(self::$dbcon->connect_error)
                die("Error de conexión (". self::$dbcon->connect_error.")");
            else{
                self::$dbcon->set_charset("utf_8");
                return self::$dbcon;
            }
       }
       public function query($consult){
           $query = self::conect()->query($consult);
           
           if($query == true){
               return $query;
           }else{
               return null;
           }
       }
       public function queryAsoc($consult){
            $vec = array();
            if($result = self::query($consult)){
                while($fila = $result->fetch_assoc()){$vec[] = $fila;}
                return $vec;
            }
       }
       public function query_row($consult){
            $vec = array();
            if($result = self::query($consult)){
                while($fila = $result->fetch_row()){
                    $vec[] = $fila;
                }
                return $vec;
            }
       }
       public function query_single_object($consult){
           if($result = self::query($consult)){
               return $result->fetch_object();
           }
       }
       public function exit_conect(){
            mysql_close(self::conect());
       }
       public function destroy(){
            return session_destroy();
       }
    }

?>