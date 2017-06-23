
function forgetCheck() {
    var xhr;
    var sid = document.getElementById("sid").value;
    var name = document.getElementById("name").value;
    var idCard = document.getElementById("idCard").value;
    if (sid == "") {
        document.getElementById("msg").innerHTML = "请输入学号";
        return false;
    } else if (name == "") {
        document.getElementById("msg").innerHTML = "请输入姓名";
        return false
    } else if (idCard == "") {
        document.getElementById("msg").innerHTML = "请输入学号";
        return false
    } else {
        forget(sid, name, idCard);
    }
}

function forget(sid, name, idCard) {
    var xhr;
    try {
        xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function(e) {
            if (this.readyState == 4) {
                if (this.status == 200) {
                    var result = JSON.parse(this.responseText);
                    if (result.code == 0) {
                        document.getElementById("msg").innerHTML = "您的密码：" + result.content;
                    } else {
                        document.getElementById("msg").innerHTML = result.message;
                        document.getElementById("password").value = "";
                    }
                } else {
                    alert("服务器异常!");
                }
            } else {
                //ignore
            }
        }
        var formData = new FormData();
        formData.append('sid', sid);
        formData.append('name', name);
        formData.append('idcard', idCard);
        xhr.timeout = 3000;
        xhr.open("POST", "forget", true);
        xhr.send(formData);
    } catch (e) {
        alert("不能创建XMLHtttpRequest对象，请更换浏览器！");
        return false;
    }
}


function check() {
	var sid = document.getElementById("sid").value;
	var pwd = document.getElementById("password").value;
	if (sid == "") {
		document.getElementById("msg").innerHTML = "请输入学号";
		return false
	} else if (pwd == "") {
		document.getElementById("msg").innerHTML = "请输入密码";
		return false
	} else {
		login(sid, pwd);
	}
}

function login(sid, pwd) {
	var xhr;
	try {
		xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function(e) {
			if (this.readyState == 4) {
				if (this.status == 200) {
					var result = JSON.parse(this.responseText);
					if (result.code == 0) {
						window.location = "course";
					} else {
						document.getElementById("msg").innerHTML = result.message;
						document.getElementById("password").value = "";
					}
				}
			} else {
				//ignore
			}
		}
		var formData = new FormData();
 		formData.append('sid', sid);
 		formData.append('password', pwd);
		xhr.timeout = 3000;
		xhr.open("POST", "login", true);		
		xhr.send(formData);
	} catch (e) {
		alert("不能创建XMLHtttpRequest对象，请更换浏览器！");
		return false;
	}
}
function validateRadio() {
	var isSelect = false;
	var courseCode;
	var courseGroup = document.getElementsByName("courseGroup");
	for (var i = 0; i <  courseGroup.length; i++) {
		if (courseGroup[i].checked) {
			isSelect = true;
			courseCode = courseGroup[i].value;
			console.log(courseCode);
			break;
		}
	}
	if (!isSelect) {
		alert("请选择一门课程");
	} else if (document.getElementById(courseCode).innerHTML == "0") {
		alert("此课余量为0\n请选择其他选修课");
	} else {
		submit(courseCode);
	}
}
function submit(courseCode) {
	var xhr;
	try {
		xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function(e) {
			if (this.readyState == 4) {
				if (this.status == 200) {
					var result = JSON.parse(this.responseText);
					alert(result.message);
					if (result.code == 0) {
						location = "success.html";
					} else if (result.code == -15) {
						location = "login.html";
					}
                }
			} else {
				//ignore
			}
		}
		var formData = new FormData();
 		formData.append('course_code', courseCode);
		xhr.timeout = 3000;
		xhr.open("POST", "submit", true);		
		xhr.send(formData);
	} catch (e) {
		alert("不能创建XMLHtttpRequest对象，请更换浏览器！");
		return false;
	}
}