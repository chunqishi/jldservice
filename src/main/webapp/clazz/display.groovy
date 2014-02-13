import groovy.json.JsonBuilder
import org.codehaus.groovy.reflection.CachedMethod
import org.jldservice.clazz.ClazzInterface
import org.jldservice.json.JsonSchema
import org.jldservice.maven.Maven
import org.jldservice.json.Json
import org.jldservice.html.Html
import groovy.xml.XmlUtil

// application
import javax.servlet.ServletContext
// request
import javax.servlet.http.HttpServletRequest
// response
import javax.servlet.http.HttpServletResponse
// session
import javax.servlet.http.HttpSession
// out
import java.io.PrintWriter

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier


def ci = new ClazzInterface()

def paraclzname = request.getParameter("clazzname");
if (paraclzname == null || "".equals(paraclzname.trim())) {
    paraclzname = ClazzInterface.getName()
}

def parajsonldid = request.getParameter("jsonldid");
if (parajsonldid == null) {
    parajsonldid = ""
}

def parajsonobj = request.getParameter("jsonobj");
if (parajsonobj == null) {
    parajsonobj = ""
}

//def paramavendep = request.getParameter("mavendep");
//def libpath = application.getRealPath("WEB-INF/lib")
//if (paramavendep == null || "".equals(paramavendep.trim())) {
//    paramavendep = "";
//} else {
//    if (!new File(libpath).exists()) {
//        new File(libpath).mkdirs()
//    }
//    new Maven().copyDependencies(paramavendep, libpath);
//}

if (!session) {
    session = request.getSession(true);
}

if (!session.counter) {
    session.counter = 1
}



def clz = this.class.classLoader.loadClass(paraclzname)
def htmlclz= JsonSchema.toJsonSchema(clz)

def htmllist = Html.listInterface(paraclzname)

def head = this.class.getResource("/head.html").text

println """<!DOCTYPE HTML>
<html>
<meta charset="utf-8" />
<head>
    <!--TODO: title -->
    <title>Display Class</title>
    <script src="http://code.jquery.com/jquery-latest.min.js"></script>
    <script type="text/javascript">
    <!--
    function escapeHTML(str) {
      return str.replace(/&/g, "&amp;").replace(/"/g, "&quot;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
    function loadSample(id, sn, mn, sample){
      var span = \$("#" + id);
      if(span.html() == "loading...")
        span.html("<iframe src='" + document.URL + "/" + sn + "?method=" + mn + "&sample=" + sample + "'></iframe>");
    }
    var ppcfg = {maxArray: 1000, maxDepth: 100, maxStringLength: 1024,
      styles: {
        array: { th:{ backgroundColor: '#CDED9A', color: '#0', "text-align": "center" } },
        'function': { th: { backgroundColor: '#D82525' } },
        regexp: { th: { backgroundColor: '#E2F3FB', color: '#000' } },
        object: { th: { backgroundColor: '#BFC6FF'/*'#1F96CF'*/, color: '#0', "text-align": "center" } },
        jquery : { th: { backgroundColor: '#FBF315' } },
        error: { th: { backgroundColor: 'red', color: 'yellow' } },
        domelement: { th: { backgroundColor: '#F3801E' } },
        date: { th: { backgroundColor: '#A725D8' } },
        colHeader: { th: { backgroundColor: '#FFFFFF'/*'#EEE'*/, color: '#000', textTransform: 'uppercase', display: "none" } },
      } };

    function invokeMethod(fn, text, sn, mn){
      var f = document.getElementById(fn);
      var n = f.elements.length;
      var s = "";
      for(i = 0; i < n; i++){
        var e = f.elements[i];
        if(s.length > 0){
          s += ",";
        }
        var v = e.value;
        if(v[0] == '{' || v[0] == '['){
          s += v;
        } else{
          s += '"' + e.value.replace(/\\"/g, "\\\\\\"") + '"';
        }
      }
      var req = '{"method": "' + mn + '", "params": [' + s + ']}';
      var start = new Date();
      \$.ajax({
        type: "POST",
        dataType: "text",
        url: document.URL + "/" + sn,
        data: req,
        success: function(data, dataType) {
          log = \$("<div></div>");
          t = \$("#" + text);
          if(t.children().length > 0){
            \$("<hr/>").insertBefore(t.children(":first"));
            log.insertBefore(t.children(":first"));
          } else{
            t.append(log);
          }
          log.append("<b>" + start + ", " + (new Date().getTime() - start.getTime()) + "msec.</b>" +
            ' <span class="info">[RAW]<span>request:<br/>' + req + "<br/><br/>response:<br/>" + data +
            "</span></span><br/><b>request:</b><br/>");
          log.append(\$(prettyPrint(jQuery.parseJSON(req), ppcfg)));
          log.append("<b>response:</b><br/>");
          log.append(\$(prettyPrint(jQuery.parseJSON(data), ppcfg)));
    //      log.css("display", "none");
    //      log.show("slide", {direction: "up"}, 1000);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown){
          var fb = "<font color=\\"red\\">";
          var fe = "</font>";
          \$("#" + text).append(
            start + ", " + (new Date().getTime() - start.getTime()) + "msec.<br/>　request: " + escapeHTML(req) +
            "<br/>status: " +
            fb + escapeHTML(textStatus) + fe + "<br/>error: " + fb + escapeHTML(errorThrown) + fe + "</font><hr/>"
            );
        }
        });
    }

    function clearLog(text){
      \$("#" + text).html("");
    }

    function togglePanel(id){
      \$("#" + id).toggle();
    }
    // -->
    </script>
    ${head}
</head>
<body>
    <a name="_top_" />
    <nav class="nav-bar">
        <a href="#jsonld-description">jsonld descirption</a>
        |
        <a href="#class-interface">class interfaces</a></nav>


<p>
</p>
<p>
${request}
</p>
<p>
${application}
</p>
<p>
Hello, ${request.remoteHost}: ${session.counter}! ${new Date()}
</p>
<p>
Welcome to Groovlets 101. As you can see
this Groovlet is fairly simple.
</p>
<p>
This course is being run on the following servlet container: </br>
${application.getServerInfo()}
</p>

<a name="jsonld-description" />
<h2>JSON-LD Descirption  <a href="#_top_" style="text-decoration: none;">^</a></h2>
<hr/>
<h3>Input of Class:</h3>
<form method="get">
<p>
    Class Name: <br/> <input name="clazzname" formmethod="get" value="${paraclzname}" size="40" style="text-align:right"/>
</p>
<p>
    JSON-LD: <br/> <textarea name="jsonldid" formmethod="get" cols="40" rows="10">${parajsonldid}</textarea>
</p>

<p>
    JSON: <br/> <textarea name="jsonobj" formmethod="get" cols="40" rows="10">${parajsonobj}</textarea>
</p>

<p>
    <input type="reset" / ><input name="clazzname" type="submit"/>
</p>
</form>


<hr/>
<h3>JSON-Schema</h3>
<p>
<a href="http://json-schema.org/latest/json-schema-core.html" target="_blank">Json schma</a> of Class ${paraclzname}:
<pre class="prettyprint linenums">
${htmlclz}
</pre>
</p>

<a name="class-interface" />
<h2>Class Interface  <a href="#_top_" style="text-decoration: none;">^</a></h2>
<p>
Original Functions of Class ${paraclzname}:
</p>

<p>
${htmllist}
</p>


<footer><hr/>
<p>
    Progress:
    <progress value="100" max="100"></progress></p>
<p>
    Contacts:
    <nonsense>diligenc</nonsense>s.cs@<nonsense>gma</nonsense>il.<nonsense></nonsense>com
</p></footer>
</body>
</html>
"""
