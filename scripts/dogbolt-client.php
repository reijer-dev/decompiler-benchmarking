<?php
function getDecompilationsUrl(){
	if (function_exists('curl_file_create')) { // php 5.5+
		$cFile = curl_file_create($_FILES['file']['tmp_name']);
	} else { // 
		$cFile = '@' . realpath($_FILES['file']['tmp_name']);
	}
	$post = array('file'=> $cFile);
	$ch = curl_init();
	//curl_setopt($ch, CURLOPT_HEADER, array('content-type: multipart/form-data; boundary=----WebKitFormBoundaryForIgBwsklr5VHBh`1'));
	curl_setopt($ch, CURLOPT_URL,"https://dogbolt.org/api/binaries/");
	curl_setopt($ch, CURLOPT_POST,1);
	curl_setopt($ch, CURLOPT_POSTFIELDS, $post);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	$url = json_decode(curl_exec($ch))->decompilations_url;
	curl_close ($ch);
	return $url;
}

function getDecompilation($url, $name){
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL,$url);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	$json = json_decode(curl_exec($ch));
	if($json->count == 0){
		return "";
	}
	$decompilations = $json->results;
	$result = array();
	foreach($decompilations as $decompilation){
		if($decompilation->decompiler->name == $name)
			return $decompilation->download_url;
	}
	curl_close ($ch);
	return null;
}

function downloadDecompilation($url){
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL,$url);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	$response = curl_exec($ch);
	curl_close ($ch);
	return $response;
}

$decUrl = getDecompilationsUrl();
$try = 0;
$downloadUrl = "";
while($downloadUrl == "" && $try < 10){
	sleep(3);
	$downloadUrl = getDecompilation($decUrl, $_GET['decompiler']);
	$try++;
}
echo downloadDecompilation($downloadUrl);
