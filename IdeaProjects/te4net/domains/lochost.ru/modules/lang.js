/**
 * @export
 */
reanet.lang = new ReaLang()
/**
 * Reanet`s lang provider
 * write {lang.item} to template
 * @constructor
 */
function ReaLang(){
    this.avilable = {}
    this.sources = {}
    this.current = {}
    this.proxy = new Proxy({}, {get: (obj, p) => {
        return p=="list"?Object.keys(this.sources):this.current[p]
    }})

    /**
     * Load lang pack from net
     * @param {string} code 
     * @param {string} url 
     * @export
     */
    this.setSrc = (code, url)=>{
        this.sources[code] = url
    }


    /**
     * Set manualy translations
     * @param {string} code 
     * @export
     */
    this.set = (code, translations)=>{
        this.sources[code] = "*"
        this.avilable[code] = translations
    }


    /**
     * Choose lang
     * @param {string} code 
     * @export
     */
    this.use = (code)=>{
        if(!reanet.get("lang")){
            reanet.lock().set("lang", this.proxy).unlock()
        }
        let lang = this.avilable[code]
        if(lang){
            this.current = lang
        } else{
            let url = this.sources[code]
            if(url){
                get(url, (data)=>{
                    let res = eval(data)
                    this.avilable[code] = res
                    this.current = lang
                    reanet.update()
                })
                return
            } else{
                this.current = {}
            }
        }
        reanet.update()
    }
}