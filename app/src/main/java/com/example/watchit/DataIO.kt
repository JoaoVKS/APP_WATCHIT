package com.example.watchit


/*class BluetoothDeviceItem (nomeInput: String, enderecoInput: String){
    val nome = nomeInput
    val endereco = enderecoInput
}*/


fun getUsers():String
{
    var retorno = "["
    retorno += "{\"id\"=1, \"nome\"=\"João Vitor\"},"
    retorno += "{\"id\"=2, \"nome\"=\"Jonathan Pereira\"}"
    retorno += "]"
    return retorno
}

fun getUser(id: String):String
{
    var retorno = "";
    if(id == "1")
        retorno  += "{\"id\"=1, \"nome\"=\"João Vitor\"}"
    if(id == "2")
        retorno += "{\"id\"=2, \"nome\"=\"Jonathan Pereira\"}"
    return retorno
}

fun login(email: String, senha: String ):String
{
    var retorno = ""

    //simula get na API
    if(email == "joaokussler@gmail.com" && senha == "123456")
        retorno = "1"

    return retorno
}
