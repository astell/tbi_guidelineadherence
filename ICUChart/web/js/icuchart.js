
var menu_status = new Array();

function select_eusig(value){
    
    //window.alert("Into select_eusig...");
    
    if (document.getElementById) {
        
        var seriesTextStr = "";
        
        var valueIn = value;
        var switch_id = null;
        var selectedId = "";
        if(valueIn===("1")){
            selectedId = "eusig_bps";
            seriesTextStr = "BPs";
        }else if(valueIn===("8")){
            selectedId = "eusig_icp";        
            seriesTextStr = "ICP";
        }else if(valueIn===("9")){
            selectedId = "eusig_cpp";        
            seriesTextStr = "CPP";
        }
        //window.alert("selectedId: " + selectedId);
        
        var allIds = new Array();
        allIds.push("eusig");
        allIds.push("eusig_bps");
        allIds.push("eusig_icp");
        allIds.push("eusig_cpp");
        
        for(var i=0; i<allIds.length; i++){
            var idIn = allIds[i];
            if(idIn===selectedId){
                switch_id = document.getElementById(selectedId);
                switch_id.className = 'show';
                menu_status[selectedId] = 'show';        
            }else{
                switch_id = document.getElementById(idIn);
                switch_id.className = 'hide';
                menu_status[idIn] = 'hide';
            }
        }
        
        document.getElementById("series_defn_disp").innerHTML="" + seriesTextStr;
    }
    return true;
}

function js_test(){
    
    return false;
}


function getEventIdList(pid){    
    
    //window.alert("into getEventIdList...");
    
    var server_root = "http://www.tbi-guidelineadherence.org";
    
    var guideline = document.getElementById("guideline_inputs").elements["guideline"].value;
    var eusigParamName = "";
    if(guideline==="1"){
        eusigParamName = "eusig_id_bps";
    }else if(guideline==="8"){
        eusigParamName = "eusig_id_icp";
    }else if(guideline==="9"){
        eusigParamName = "eusig_id_cpp";
    }
    var eusig_id = document.getElementById("guideline_inputs").elements[eusigParamName].value;
    var holddown = document.getElementById("guideline_inputs").elements["holddown"].value;
    
    //window.alert("eusig_id: " + eusig_id);
    //window.alert("holddown: " + holddown);
    
    var xmlhttp;
    if (pid==="" || eusig_id==="" || holddown===""){
        document.getElementById("event_id_list").innerHTML="";
        return;
    }
  
    if (window.XMLHttpRequest){
        // code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
    }else{
        // code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
    }
  
    xmlhttp.onreadystatechange=function(){
        if (xmlhttp.readyState===4 && xmlhttp.status===200){
            document.getElementById("event_id_list").innerHTML=xmlhttp.responseText;
        }        
    };
    
    var script_url = server_root + "/geteventidlist.jsp?pid=" + pid + "&eusig_id=" + eusig_id + "&holddown=" + holddown;
    xmlhttp.open("POST",script_url,true);
    xmlhttp.send();
}


