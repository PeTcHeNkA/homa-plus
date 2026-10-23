package tutil

import (
	"encoding/json"
	"os"
	"os/exec"
	"strings"
)

type LArgs struct {
	Cp        string
	Nlib      string
	Sysloader string
	Lars      string
	Ncp       string
	Natives   string
}

func item(root string, item0 string) string {
	if item0 != "" {
		return strings.ReplaceAll(root+"\\"+item0, "/", "\\")
	}
	return strings.ReplaceAll(root, "/", "\\")
}

func ncp(root string, nlcp string) string {
	args := strings.Split(nlcp, ";")
	for c, it := range args {
		args[c] = item(root, it)
	}
	return strings.Join(args, ";")
}

func cmd(root string, largs *LArgs, name *string, uuid *string, token *string) []string {
	var args []string

	args = append(args, "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=55700")

	args = append(args, "-cp", item(root, largs.Cp))
	args = append(args, "-Djava.system.class.loader=plus.nlaunch.NULaunch")
	args = append(args, "-Dnlaunch.lib="+item(root, largs.Nlib))
	args = append(args, "-Dnlaunch.lars="+item("!"+root, largs.Lars))
	args = append(args, "-Dnlaunch.cp="+ncp(root, largs.Ncp))
	args = append(args, "-Djava.library.path="+item(root, largs.Natives))
	args = append(args, "-Xss2M", "net.minecraft.launchwrapper.Launch")
	args = append(args, "--tweakClass", "net.minecraftforge.fml.common.launcher.FMLTweaker", "--versionType", "Forge")
	args = append(args, "--gameDir", item(root, ""))
	args = append(args, "--assetsDir", item(root, "assets"))
	args = append(args, "--assetIndex", "1.12")

	if name != nil && *name != "" {
		args = append(args, "--username", *name)
	}
	if uuid != nil && *uuid != "" {
		args = append(args, "--uuid", *uuid)
	}
	if token != nil && *token != "" {
		args = append(args, "--userType", "mojang", "--accessToken", *token)
	} else {
		args = append(args, "--accessToken", "offline-token")
	}
	return args
}

func Launch0(java string, root string, args *LArgs, name *string, uuid *string, token *string) {
	command := cmd(item(root, ""), args, name, uuid, token)
	println("\"" + item(java, "") + "\" " + strings.Join(command, " "))
	cmd := exec.Command(item(java, ""), command...)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	err := cmd.Start()
	if err != nil {
		println("CRITICAL ERROR\n", err.Error())
		return
	}
}

func readArgs(jsonStr string) *LArgs {
	var args LArgs
	err := json.Unmarshal([]byte(jsonStr), &args)
	if err != nil {
		println("CRITICAL ERROR\n", err)
		return nil
	}
	return &args
}

func Launch1(java string, root string, jsonStrArgs string, name *string, uuid *string, token *string) {
	Launch0(java, root, readArgs(jsonStrArgs), name, uuid, token)
}

func Launch(root string, jsonStrArgs string, name *string, uuid *string, token *string) {
	Launch1(root+"/java/bin/java.exe", root, jsonStrArgs, name, uuid, token)
}
