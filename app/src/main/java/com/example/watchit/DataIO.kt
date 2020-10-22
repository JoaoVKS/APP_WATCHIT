package com.example.watchit

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.lang.Exception
import java.security.MessageDigest
import java.time.LocalDateTime
import kotlin.random.Random

@Serializable
data class User (
    var aditional_infos : String,
    var email : String,
    var first_name : String,
    var birthday : String,
    var id : Int = 0,
    var last_name : String,
    var password : String
)


fun sendJsonData(url: String, data: String, idUser: Int, idCategory: Int)
{
    val currentDateTime = LocalDateTime.now().toString()
    val bodyJson =
        """
        {
        "data" : "$data",
        "date" : "$currentDateTime",
        "id_category" : $idCategory,
        "id_user" : $idUser
        }
        """
    Fuel.post(url)
        .body(bodyJson)
        .response { result -> }
}

fun rand(start: Int, end: Int): Int {
    require(!(start > end || end - start + 1 > Int.MAX_VALUE)) { "Illegal Argument" }
    return Random(System.nanoTime()).nextInt(end - start + 1) + start
}

fun hashPassword(password: String):String?{
    return hashString("SHA-1", password)
}

private fun hashString(type: String, input: String): String {
    val HEX_CHARS = "0123456789ABCDEF"
    val bytes = MessageDigest
        .getInstance(type)
        .digest(input.toByteArray())
    val result = StringBuilder(bytes.size * 2)

    bytes.forEach {
        val i = it.toInt()
        result.append(HEX_CHARS[i shr 4 and 0x0f])
        result.append(HEX_CHARS[i and 0x0f])
    }

    return result.toString()
}

private fun getUsersJson():String
{
    //DADOS FALSOS TEMPORARIOS PARA SIMULAR RETORNO DA API
    var jsonstring = ""
    jsonstring += "{\"aditional_infos\":\"bronquite crônica\", \"birthday\":\"1995-12-09\", \"email\":\"jonathanb@hotmail.com\", \"first_name\":\"Jonathan\", \"id\":1, \"last_name\":\"Bockorny\", \"password\":\"7c4a8d09ca3762af61e59520943dc26494f8941b\"},"
    jsonstring += "{\"aditional_infos\":\"informacao extra teste\", \"birthday\":\"1998-03-15\", \"email\":\"joaokussler@gmail.com\", \"first_name\":\"João Vitor\", \"id\":2, \"last_name\":\"Kussler\", \"password\":\"7c4a8d09ca3762af61e59520943dc26494f8941b\"}"
    return jsonstring
}

fun getUsers():  ArrayList<User>
{
    var jsonstring = "[" + getUsersJson() + "]"
    var jsonLista = Json.parseToJsonElement(jsonstring).jsonArray
    var usuarios: ArrayList<User> = arrayListOf()
    try {
        for(itemlista in jsonLista)
        {
            var item: JsonObject = itemlista.jsonObject
            var usr: User = User("", "", "", "", 0, "", "")
            usr.aditional_infos = item["aditional_infos"].toString().replace("\"","")
            usr.email = item["email"].toString().replace("\"","")
            usr.first_name = item["first_name"].toString().replace("\"","")
            usr.birthday = item["birthday"].toString().replace("\"","")
            usr.last_name = item["last_name"].toString().replace("\"","")
            usr.id = item["id"]!!.toString().toInt()
            usr.password = item["password"].toString().replace("\"","")
            usuarios.add(usr)
        }
    }
    catch (ex: Exception){

    }
    return usuarios
}


fun getUser(id: Int): User?
{
    var retorno: User? = User("","","","",0,"","")
    val usuarios = getUsers()
    if (usuarios != null) {
        if(usuarios.count() > 0) {
            var usuario = usuarios!!.find { x -> x.id == id }
            if(usuario != null) {
                retorno = usuario
            }
        }
    }

    return retorno
}

fun login(email: String, senha: String ): Int
{
    var retorno = 0

    //simula get na API
    val usuarios = getUsers()
    var senhacodificada = hashPassword(senha).toString().toLowerCase()
    val logado = usuarios!!.find { x -> x.email.toLowerCase() == email && x.password == senhacodificada }
    if(logado != null)
    {
        retorno = logado.id
    }

    return retorno
}
