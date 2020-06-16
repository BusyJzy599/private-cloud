/*全局变量*/
let file = "";
let files = new Array();
Array.prototype.pushNoRepeat = function () {
    for (var i = 0; i < arguments.length; i++) {
        var ele = arguments[i];
        if (this.indexOf(ele) == -1) {
            this.push(ele);
        }
    }
};

/**
 * 点击新建文件夹
 */
function addDir() {
    var element = document.getElementById("newDir");
    var newName = document.getElementById("addNew");
    element.className = "panel panel-info filePanel"
    newName.focus()
}

/**
 * 增加文件失焦状态
 */
function hidePanel() {
    var element = document.getElementById("newDir");
    element.className = "panel panel-info filePanel hidden"
}

/**
 * 全选/取消按钮
 */
function selectAll() {
    var element = document.getElementById("chooseSpan");
    var fs = document.getElementsByName("file");
    var de = document.getElementById("delete");
    if (element.className == "glyphicon glyphicon-ok") {
        element.className = "glyphicon glyphicon-remove"
        element.textContent = "取消"
        de.classList.remove("hidden")
        for (var i = 0; i < fs.length; i++)
            fs[i].checked = true;
    } else {
        element.className = "glyphicon glyphicon-ok"
        element.textContent = "全选"
        de.classList.add("hidden")
        for (var i = 0; i < fs.length; i++)
            fs[i].checked = false;
    }
}

/**
 * 选择文件
 */
function selectFile() {
    var fs = document.getElementsByName("file");
    var de = document.getElementById("delete");
    for (var i = 0; i < fs.length; i++) {
        if (fs[i].checked) {
            de.classList.remove("hidden")
            return
        }
    }
    de.classList.add("hidden")
}

/**
 * 修改文件名
 */
function changeName() {
    var newName = $("#changeFileName").val();
    console.log(file)

    if (newName.trim() === "") {
        $("#changeFileForm").addClass("has-error")
    } else {
        $("#changeFileForm").removeClass("has-error")
        $.ajax({
            type: "POST",
            url: "/local/change",
            dataType: "json",
            contentType: "application/json;charset=utf-8",
            /*获取的数据*/
            data: JSON.stringify({
                "oldName": file,
                "newName": file.substring(0, file.lastIndexOf("/") + 1) + newName,
            }), success: function (response) {
                if (response.code != 200) {
                    alert(response.message);
                }
            }, complete: function (options) {
                $('#changeName').modal('hide')
                $('#success').modal('show')
                setTimeout(function () {
                    console.log("加载")
                    location.reload();
                }, 1000);
            }
        });

    }
}

/**
 * 跳转路径或显示预览
 */
function chooseFile(e) {
    var pathName = e.getAttribute("data-path");
    var type = e.getAttribute("data-type");
    var url = window.location.href.substring(0, window.location.href.length - window.location.search.length)
    if (type == "dir")
        window.location.href = url + "?hdfs=" + pathName
    else {
        $('#content').hide()
        $("#loading").show()
        $('#content').attr('src','http://localhost:8887/local/toPdfFile?path='+pathName);
         let doc = document.getElementById('content')
         doc.onload = function(){
             let iframe = doc.contentWindow;
             $("#loading").hide()
             $('#content').show()

         };
        $('#showFile').modal('show')
    }

}

/**
 * 获取数据
 * @param e
 */
function getData(e) {
    var pathName = e.getAttribute("data-path");
    file = pathName;
}

/**
 * 取消按钮
 */
function cancelDelete() {
    file = "";
    files = new Array()
}

/**
 * 删除文件
 */
function deleteFile() {
    if (file == "") {
        var fs = document.getElementsByName("file");
        for (var i = 0; i < fs.length; i++) {
            if (fs[i].checked) {
                files.pushNoRepeat(fs[i].getAttribute("data-path"))
            }
        }
        $.ajax({
            type: "POST",
            url: "/local/deleteFiles",
            dataType: "json",
            contentType: "application/json;charset=utf-8",
            /*获取的数据*/
            data: JSON.stringify({
                "pathName": files
            }), beforeSend: function (XMLHttpRequest) {
                //alert('远程调用开始...');
                console.log("正在删除")
            }, success: function (response) {
                console.log("成功进入");
            },
            complete: function (options) {
                $('#deleteFile').modal('hide')
                $('#success').modal('show')
                setTimeout(function () {
                    console.log("加载")
                    location.reload();
                }, 1000);
            }
        });
    } else {
        $.ajax({
            type: "POST",
            url: "/local/delete",
            dataType: "json",
            contentType: "application/json;charset=utf-8",
            /*获取的数据*/
            data: JSON.stringify({
                "pathName": file
            }),
            success: function (response) {
                console.log("成功进入");
                if (response.code != 200) {
                    alert(response.message);
                }
                $('#deleteFile').modal('hide')
                $('#success').modal('show')
                setTimeout(function () {
                    console.log("加载")
                    location.reload();
                }, 1000);

            },
        });
    }
}

/**
 * 获取上传文件
 */
function getFile() {
    var process = $("#process");
    process.addClass("in")
    process.css("display", "block");
    var fd = new FormData($("#form_table")[0]);
    var file = document.getElementById("lefile");
    /*获取文件名*/
    var filename = file.value;
    var pos = filename.lastIndexOf("\\");
    filename = filename.substring(pos + 1);
    if (filename != "") {
        $("#upload").modal("hide")
        $.ajax({
            type: "POST",
            url: "/local/upload",
            dataType: "json",
            processData: false,
            contentType: false,
            /*获取的数据*/
            data: fd,
            xhr: function () {
                myXhr = $.ajaxSettings.xhr();
                if (myXhr.upload) { // check if upload property exists
                    myXhr.upload.addEventListener('progress', function (e) {
                        var loaded = e.loaded;//已经上传大小情况
                        var tot = e.total;//附件总大小
                        var per = Math.floor(100 * loaded / tot);  //已经上传的百分比
                        $("#processing").css("width", per + "%");
                        $("#processing").html(per + "%");
                        console.log('附件总大小 = ' + tot);
                        console.log('已经上传大小 = ' + loaded);
                    }, false); // for handling the progress of the upload
                }
                return myXhr;
            },
            success: function (response) {
                if (response.code != 200) {
                    alert(response.message);
                }
                $('#success').modal('show')
            }, error: function () {
                $('#failed').modal('show')
                $('#message').html("上传失败")
            }, complete: function () {
                process.removeClass("in")
                process.css("display", "none");
                setTimeout(function () {
                    $('#failed').modal('hide')
                    $('#success').modal('hide')
                    location.reload()
                }, 1000);
            }
        });
    }

}

/**
 * 获取文件名
 * @param e
 */
function showFileName() {
    /*获取文件名*/
    var filename = document.getElementById("lefile").value;
    var pos = filename.lastIndexOf("\\");
    filename = filename.substring(pos + 1);
    $("#fileName").html(filename);
}
