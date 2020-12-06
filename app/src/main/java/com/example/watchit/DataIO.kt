package com.example.watchit

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import java.lang.Exception
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.O)
fun sendJsonData(url: String, data: String, idUser: Int, idCategory: Int)
{
    val currentDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now().toString()
    } else {
        TODO("VERSION.SDK_INT < O")
        ""
    }
    var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    var formatted = currentDateTime.format(formatter).replace("T", " ").split('.').first()
    val bodyJson = "{\"id_user\" : \"${idUser}\",\"date\" : \"${formatted}\",\"data\" : \"${data}\"}"
    Fuel.post(url)
        .header("Content-type" to "application/json")
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
    Fuel.get("http://emerghelp1.pythonanywhere.com/selectJson")
        .response { request, response, result ->
            when(result)
            {
                is Result.Success -> {
                    jsonstring = String(response.data).toString()
                }
            }
        }
    while(jsonstring.isNullOrEmpty())
    {
        Thread.sleep(100)
    }
    return jsonstring
}

fun getUsers():  ArrayList<User>
{
    var jsonstring = getUsersJson()
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
fun getUser(email: String): User?
{
    var retorno: User? = User("", "", "", "", 0, "", "")
    val usuarios = getUsers()
    if (usuarios != null) {
        if(usuarios.count() > 0) {
            var usuario = usuarios!!.find { x -> x.email.equals(email) }
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

fun cadastro(usuario: User): Int
{
    var retorno:Int = -1
    //simula get na API
    val usuarios = getUsers()
    val existe = usuarios!!.find { x -> x.email.toLowerCase() == usuario.email.toLowerCase()}
    if(existe != null)
    {
        return -2
    }
    var senhacodificada = hashPassword(usuario.password).toString().toLowerCase()
    var jsonsend = "{ \"first_name\": \"${usuario.first_name}\", \"last_name\": \"${usuario.last_name}\", \"birthday\": \"${usuario.birthday.replace('/', '-')}\", \"email\": \"${usuario.email}\", \"password\":\"${usuario.password}\", \"aditional_infos\": \"${usuario.aditional_infos}\" }"
    retorno = postCadastro(jsonsend)
    return retorno
}

fun postCadastro(bodyJson: String): Int
{
    var retorno = -1
    var tmp = ""
    //Faz post com o json do usuário para cadastrar na API
    Fuel.post("http://emerghelp1.pythonanywhere.com/insert")
        .header("Content-type" to "application/json")
        .body(bodyJson)
        .response { request, response, result ->
            when (result) {
                is Result.Failure ->
                {
                    tmp = request.toString()
                    tmp = response.toString()
                    tmp = result.toString()
                    retorno = 0
                }
                is Result.Success ->
                {
                    //VALIDAR COM O JONATHAN O RETORNO DA API
                    var resp = Json.decodeFromString<UserResponse>(result.get().toString())
                    if(resp != null && resp.id > 0)
                    {
                        retorno = resp.id
                    }
                    retorno = 1
                }
            }
        }
    while(retorno < 0)
    {
        Thread.sleep(10)
    }

    return  retorno
}

@Serializable
data class UserResponse(var aditional_infos : String = "",
                        var birthday : String = "",
                        val email : String = "",
                        var first_name : String = "",
                        var id : Int =  0,
                        var last_name : String = "",
                        var password : String = "")
{

}
