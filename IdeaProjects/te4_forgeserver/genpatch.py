import os, json
dir = "C:/Users/Te4hnoPlus/IdeaProjects/te4_forgeserver/"

__mainPath__      = (__file__.replace("\\", "/")[0:__file__.rfind("/")+1])
__logField__ = ""
__basicPrinter__ = print
__basicInputer__ = input
__defFormat__ = " > {value}"
__canAdd__ = False
__codecs__ = ["cp1252", "cp437", "utf-16be", "utf-16", "ascii", "utf-8"]


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


def createForFileIfNeed(path):
    pt = path.split("/")
    del pt[len(pt)-1]
    pt2 = __mainPath__+("/".join(pt))
    if not os.path.exists(pt2+"/"):
        pass
        os.makedirs(pt2)


def createFile(path, value="", encoding=5):
    enc = encoding
    if type(enc) == tuple:
        enc = encoding[0]

    createForFileIfNeed(path)
    with open(__mainPath__+path, "w+", encoding=__codecs__[enc]) as file:
        if type(value) == dict:
            import json
            indent = None;
            sort = False
            if type(encoding) == tuple:
                indent = encoding[1]
                sort = encoding[2]
            json.dump(value, file, indent=indent, sort_keys=sort)
        else:
            file.write(value)
            file.close()


def makePathFile():
    s = ""
    for item in getDeepFiles(dir+"build/classes/java/main/net"):
        name = "net."+item.replace("/",".")[0:-6]
        s += f"\"{name}\"\n"

    createFile("src/main/resources/elcapatch.csv", s, )
    print("Completed.")


if __name__ == "__main__":makePathFile()

# print("\nprivate void addPatch(){")
# for item in getDeepFiles(dir+"build/classes/java/main/net"):
#     name = "net."+item.replace("/",".")[0:-6]
#     print(f"add(\"{name}\");")
# print("}\n")