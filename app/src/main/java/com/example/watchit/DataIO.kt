package com.example.watchit

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import java.lang.Exception
import java.security.MessageDigest
import java.time.LocalDateTime
import kotlin.coroutines.coroutineContext
import kotlin.random.Random



fun getBPM():Int {
    //simula o que seria a conexão/coleta de dados com o smartwatch
    return rand(71, 99)
}

fun sendJsonData(url: String, data: String, idUser: Int, idCategory: Int)
{
    val currentDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now().toString()
    } else {
        TODO("VERSION.SDK_INT < O")
        ""
    }
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
    jsonstring += "{\"aditional_infos\":\"Lorem ipsum\", \"birthday\":\"1995-12-09\", \"email\":\"jonathanb@hotmail.com\", \"first_name\":\"Jonathan\", \"id\":1, \"last_name\":\"Bockorny\", \"password\":\"7c4a8d09ca3762af61e59520943dc26494f8941b\"},"
    jsonstring += "{\"aditional_infos\":\"Lorem ipsum\", \"birthday\":\"1998-03-15\", \"email\":\"joaokussler@gmail.com\", \"first_name\":\"João Vitor\", \"id\":2, \"last_name\":\"Kussler\", \"password\":\"7c4a8d09ca3762af61e59520943dc26494f8941b\"}"
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
    var retorno: User? = User("", "", "", "", 0, "", "")
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

fun cadastro(usuario: User): String
{
    var retorno = ""
    //simula get na API
    val usuarios = getUsers()
    val existe = usuarios!!.find { x -> x.email.toLowerCase() == usuario.email.toLowerCase()}
    if(existe != null)
    {
        return "E-mail já cadastrado"
    }
    var senhacodificada = hashPassword(usuario.password).toString().toLowerCase()
    var tempJlement = Json.encodeToJsonElement(usuario)
    retorno = tempJlement.toString()
    postCadastro(retorno)
    return retorno
}

fun postCadastro(bodyJson: String): Int
{
    var retorno = 0
    //Faz post com o json do usuário para cadastrar na API
    Fuel.post("url")
        .body(bodyJson)
        .response { result ->
            when (result) {
                is Result.Failure ->  retorno = 0
                is Result.Success ->
                {
                    //VALIDAR COM O JONATHAN O RETORNO DA API
                    var resp = Json.decodeFromString<UserResponse>(result.get().toString())
                    if(resp != null && resp.id > 0)
                    {
                        retorno = resp.id
                    }
                }
            }
        }

    return  retorno
}
