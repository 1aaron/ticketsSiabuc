<?php
    require_once("controllers/User.php");
    require_once("controllers/Ticket.php");
    require_once("controllers/Historial.php");
    require_once("controllers/Usuario.php");
    require_once("controllers/Estadistica.php");    
    
    const UNKNOWN_URL = "url desconocida";
    error_reporting(E_ERROR | E_WARNING | E_PARSE);

    if(isset($_GET['PATH_INFO'])){
        $parametros = explode('/', $_GET['PATH_INFO']);
    }else{
        echo json_encode(array('status' => UNKNOWN_URL),JSON_PRETTY_PRINT);
    }

    $recurso = $parametros[0];
    $recursos_existentes = array('user','ticket','historial','usuario','estadistica');

    if(!in_array($recurso,$recursos_existentes)){
        return array('status' => UNKNOWN_URL );
    }

    $recurso = ucfirst($recurso);
    $metodo = strtolower($_SERVER['REQUEST_METHOD']);

    switch($metodo){
        case 'post':
        case 'get':
            if(method_exists($recurso,$metodo)){                
                $respuesta = call_user_func(array($recurso,$metodo),$parametros);
                echo json_encode($respuesta,JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
                break;
            }
        default:
            return array("status",UNKNOWN_URL);
    }
?>