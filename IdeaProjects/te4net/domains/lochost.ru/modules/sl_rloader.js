/**
 * Loads templates from the current page
 */

let tms = document.getElementsByTagName("template")
for(let i = 0; i < tms.length; i++){
    reanet.load(tms[i].getAttribute("name"), tms[i].innerHTML)
}