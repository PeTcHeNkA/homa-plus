import {
    Minimize, Quit, GetFiles, GetLaunchPatch, ExplorerSelect, ClickPlay,
    GetCache, SetCache
} from '../wailsjs/go/main/App.js';

window.Minimize       = Minimize
window.Quit           = Quit
window.GetFiles       = GetFiles
window.GetLaunchPatch = GetLaunchPatch
window.ExplorerSelect = ExplorerSelect
window.ClickPlay      = ClickPlay
window.GetCache       = GetCache
window.SetCache       = SetCache
window.onloads        = []

document.addEventListener("wheel", event => {
    const { ctrlKey } = event;
    if (ctrlKey) {
       event.preventDefault();
       return
    }
}, { passive: false });

GetLaunchPatch().then((res)=>{
    window.launchPatch = res
})
