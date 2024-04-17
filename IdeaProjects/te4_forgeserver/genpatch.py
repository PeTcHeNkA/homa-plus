import os
dir = "C:/Users/Te4hnoPlus/IdeaProjects/te4_forgeserver/"


def getDeepFiles(pth, prefix = "", ignore=[]):
    fls = os.listdir(pth)
    fls2 = []
    for f in fls:
        if f in ignore:
            continue
        if "." in (pth+f):
            fls2.append(prefix+f)
        else:
            newdir = (pth+"/"+f).replace("//", "/")
            if prefix+f+"/" in ignore:
                continue
            fls3 = getDeepFiles(newdir, prefix+f+"/")
            for ff in fls3:
                fls2.append(ff)
    return fls2


print("\nprivate void addPatch(){")
for item in getDeepFiles(dir+"build/classes/java/main/net"):
    name = "net."+item.replace("/",".")[0:-6]
    print(f"add(\"{name}\");")
print("}\n")