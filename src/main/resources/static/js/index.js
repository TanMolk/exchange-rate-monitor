const mailReg = new RegExp("^[a-zA-Z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$");
const numReg = new RegExp("^\\d+(\\.\\d+)?$");


function validSubscribe() {
    let form = document.getElementById("contact");
    let mail = document.getElementById("mail");
    let num = document.getElementById("currency");

    if (!mailReg.test(mail.value)) {
        alert("邮箱格式不合法，请重新输入");
        return;
    } else if (!numReg.test(num.value)) {
        alert("汇率提醒值不合法，请重新输入");
        return;
    }
    form.action = "./register"
    form.submit();

}

function validUpdate() {
    let form = document.getElementById("contact");
    let mail = document.getElementById("mail");
    let num = document.getElementById("currency");
    let bank = document.getElementById("bank-select");

    if (!mailReg.test(mail.value)) {
        alert("邮箱格式不合法，请重新输入");
        return;
    }
    if (isEmpty(num.value) && isEmpty(bank.value)){
        alert("必须有一个更新值");
        return;
    }

    form.action = "./register"
    form.submit();
}

function unsubscribeHandler() {
    let form = document.getElementById("contact");
    let mail = document.getElementById("mail");

    if (!mailReg.test(mail.value)) {
        alert("邮箱格式不合法，请重新输入");
        return;
    }

    form.action = "./unsubscribe"
    form.method = 'get'
    form.submit();
}

//判断字符是否为空的方法
function isEmpty(obj){
    return typeof obj == "undefined" || obj == null || obj === "";
}