using System.Net;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Text.RegularExpressions;


namespace GF2_LaunchHX.lib
{
    public class NeiroTranslate : ITranslateEngine
    {
        //public string langKey;
        public string systemPrompt;
        public Dictionary<string, Definition[]> definitions = new Dictionary<string, Definition[]>();
        public string definitionsHeader;
        public int maxLen = 4096;
        public string LMStudioHostURL;
        public string LMStudioModelDownloadURL;
        public string LMStudioModelID;
        private string instanceId;

        public bool Setup(MainWindow window, LangTable tb, TranslationCallback cb)
        {
            try
            {
                SetupModel();
            }
            catch
            {
                return false;
            }
            return true;
        }


        public bool Finish()
        {
            using (HttpClient client = new HttpClient()) 
            {
                UnloadModel(client);
            }
            return true;
        }


        public void SetupModel()
        {
            using (var client = new HttpClient())
            {
                DownloadModelRequest request = new();
                request.model = LMStudioModelDownloadURL;
                //request.quantization = LMStudioModelQuant;

                var rsp = Send<DownloadModelResponse>(client, HttpMethod.Post, LMStudioHostURL+"/api/v1/models/download", request);

                if(rsp.status != "already_downloaded" && rsp.job_id != null)
                {
                    var dStatus = "downloading";
                    while(dStatus == "downloading")
                    {
                        Thread.Sleep(1000);

                        var resp2 = GetModelStatus(client, rsp.job_id);
                        dStatus = resp2.status;
                    }
                }

                LoadModelRequest request1 = new();
                request1.model = LMStudioModelID;
                request1.context_length = maxLen;

                var rsp2 = Send<LoadModelResponse>(client, HttpMethod.Post, LMStudioHostURL + "/api/v1/models/load", request1);

                if (!rsp2.IsLoaded()) throw new Exception("Model not loaded!");
                instanceId = rsp2.instance_id;
            }
        }


        public void UnloadModel(HttpClient client)
        {
            UnloadModelRequestResponce req = new();
            req.instance_id = instanceId;
            Send<UnloadModelRequestResponce>(client, HttpMethod.Post, LMStudioHostURL+ "/api/v1/models/unload", req);
        }


        public bool TranslateLines(List<ELine> lines, TranslationCallback cb)
        {
            StringBuilder sbRequest = new StringBuilder((int)(maxLen * 1.1));

            int cursor = 0;

            using (var httpClient = new HttpClient())
            {

                while (cursor < lines.Count)
                {
                    int newCursor = CreateRequest(sbRequest, cursor, maxLen, lines);
                    if (cursor != newCursor)
                    {

                        var rq = CreateHttpRequest(sbRequest.ToString());
                        sbRequest.Clear();

                        var t = httpClient.Send(rq);
                        var res = t.Content.ReadAsStringAsync();
                        res.Wait();

                        var rt0 = JsonSerializer.Deserialize<MResponse>(res.Result);

                        var translated = rt0.GetMsgContent();

                        //cb._status = rt0.GetReasoningContent();
                        cb.cur += newCursor - cursor;

                        List<TLine> nLines = Parse(translated);
                        for(int i = 0; i < nLines.Count; i++)
                        {
                            lines[cursor + nLines[i].line-1].Value = nLines[i].text;
                        }


                        cursor = newCursor;
                    }
                    else break;
                }
            }

            cb.active = true;
            return true;
        }


        static List<TLine> Parse(string input)
        {
            var list = new List<TLine>();

            foreach (Match match in Regex.Matches(input, "(\\d+):\\s+\"([^\"]+)\""))
            {
                var line = new TLine
                {
                    line = int.Parse(match.Groups[1].Value),
                    text = match.Groups[2].Value
                };

                list.Add(line);
            }

            return list;
        }


        public string? SingleTranslate(string text)
        {
            StringBuilder sbRequest = new();
            ELine ln = new();
            ln.Original = text;
            AddLine(sbRequest, 0, ln);
            using (var httpClient = new HttpClient())
            {
                var rq = CreateHttpRequest(sbRequest.ToString());
                sbRequest.Clear();

                var t = httpClient.Send(rq);
                var res = t.Content.ReadAsStringAsync();
                res.Wait();

                var rt0 = JsonSerializer.Deserialize<MResponse>(res.Result);
                return rt0.GetMsgContent();
            }
            return null;
        }


        public O Send<O>(HttpClient client, HttpMethod mtd, Object input)
        {
            return Send<O>(client, mtd, LMStudioHostURL + "/api/v1/chat", input);
        }


        private static JsonSerializerOptions SEND_SETTINGS = new JsonSerializerOptions() {IgnoreNullValues = true};
        public O Send<O>(HttpClient client, HttpMethod mtd, string url, Object input)
        {
            HttpRequestMessage rq = new(HttpMethod.Post, url);

            if (input != null)
            {
                string strInput;
                if(input is string)
                {
                    strInput = input as string;
                }
                else
                {
                    strInput = JsonSerializer.Serialize(input, SEND_SETTINGS);
                }
                rq.Content = new StringContent(strInput, Encoding.UTF8, "application/json");
            }

            var t = client.Send(rq);

            var res = t.Content.ReadAsStringAsync();
            res.Wait();

            return JsonSerializer.Deserialize<O>(res.Result);
        }


        public HttpRequestMessage CreateHttpRequest(string request)
        {
            HttpRequestMessage msg = new(HttpMethod.Post, LMStudioHostURL + "/api/v1/chat");
            msg.Content = new StringContent(CreateJsonRequest(request),Encoding.UTF8, "application/json");
            return msg;
        }


        private string CreateJsonRequest(string request)
        {
            MRequest mRequest = new();
            mRequest.model = LMStudioModelID;
            mRequest.input = request;

            var werbs = request.Split(" ").Select(wb => wb.Trim().ToLower()).Where(wb => wb != "").ToHashSet();

            StringBuilder sb = new StringBuilder();

            foreach (var werb in werbs)
            {
                if (definitions.ContainsKey(werb))
                {
                    foreach (var def in definitions[werb]) 
                    {
                        sb.Append(def.text).Append(": ")
                            .Append("\"").Append(def.translate).Append("\" (")
                            .Append(def.discription).Append(")\n");
                    }
                }
            }
            var sysPromt = systemPrompt;

            if (sb.Length > 0) 
            {
                sysPromt = sysPromt + "\n" + definitionsHeader + "\n" + sb.ToString();
            }

            mRequest.system_prompt = sysPromt;

            return JsonSerializer.Serialize(mRequest);
        }


        private static int CreateRequest(StringBuilder sbRequest, int cursor, int maxLen, List<ELine> lines)
        {
            int countLines = 1;

            while (sbRequest.Length < maxLen && cursor < lines.Count) {
                int preLine = sbRequest.Length;

                AddLine(sbRequest, countLines, lines[cursor]);

                if(sbRequest.Length > maxLen)
                {
                    sbRequest.Length = preLine;
                    --cursor;
                    break;
                }
                ++cursor;
                ++countLines;
            }
            return cursor;
        }


        private static void AddLine(StringBuilder sb, int num, ELine line)
        {
            sb.Append(num.ToString()).Append(": \"").Append(line.Original).Append("\"\n");
        }
    
    
        private DownloadModelStatusResponse GetModelStatus(HttpClient client, string job_id)
        {
            return Send<DownloadModelStatusResponse>(client, HttpMethod.Get, LMStudioHostURL + "/api/v1/models/download/status/"+job_id);
        }

    }


    public class Definition
    {
        public string[]? keys {  get; set; }
        public string text { get; set; }
        public string translate { get; set; }
        public string? discription { get; set; }
    }


    class MRequest
    {
        public string model { get; set; }
        public string? system_prompt { get; set; }
        public string input { get; set; }
        public string reasoning { get; set; }
        public bool store { get; set; } = false;
        public MPlugin[]? integrations { get; set; }
    }


    public class MPlugin
    {
        public string type { get; set; } = "plugin";
        public string id { get; set; }
        public string[] allowed_tools { get; set; }
    }


    class MResponse
    {
        public string model_instance_id { get; set; }
        public string response_id { get; set; }
        public List<MResponseOutMsg> output { get; set; }

        public string GetMsgContent()
        {
            return GetTypedContent("message");
        }


        public string GetReasoningContent()
        {
            return GetTypedContent("reasoning");
        }


        private string GetTypedContent(string type)
        {
            var res = output.Where(msg => msg.type == type);
            if (res.Count() == 0) return "";
            if (res.Count() > 1)
            {
                return string.Join("\n", res.Select((m) => m.content));
            }
            else
            {
                return res.First().content;
            }
        }
    }


    class MResponseOutMsg
    {
        public string type { get; set; }
        public string? content { get; set; }
        public string? tool { get; set; }
        public Dictionary<string,string> arguments { get; set; }
    }


    class DownloadModelRequest //POST
    {
        public string model { get; set; }
        public string? quantization { get; set; }
    }


    class DownloadModelResponse
    {
        public string job_id { get; set; }
        public string status { get; set; }//: "downloading" | "paused" | "completed" | "failed" | "already_downloaded"
        public string? completed_at { get; set; }
        public int total_size_bytes { get; set; }
        public string? started_at { get; set; } //ISO 8601 format
    }


    class LoadModelRequest
    {
        public string model {  set; get; }
        public int context_length { set; get; } = 4096;
        public int? eval_batch_size { set; get; }
        public bool flash_attention {  set; get; } = true;
        public int? num_experts { set; get; }
        public bool offload_kv_cache_to_gpu { set; get; } = true;
    }


    class LoadModelResponse
    {
        public string type { set; get; }
        public double load_time_seconds { set; get; }
        public string instance_id { set; get; }
        public string status { set; get; }

        public bool IsLoaded()
        {
            return status == "loaded";
        }
    }


    class UnloadModelRequestResponce
    {
        public string instance_id {  get; set; }
    }


    class DownloadModelStatusResponse
    {
        public string job_id {  set; get; }
        public string status { set; get; } //"downloading" | "paused" | "completed" | "failed"
        public int total_size_bytes {  set; get; }
        public int downloaded_bytes { set; get; }
        public string started_at { set; get; }
        public string completed_at { set; get; }
    }



    class TLine
    {
        public int line { set; get; }
        public string text { set; get; }
    }


    public class NeirTranslateOptions
    {
        //[JsonIgnore]
        //public string lang_key { set; get; }
        public string lm_studio_api_host { set; get; } = "http://127.0.0.1:1234";
        public string model_id { set; get; }
        public string model_url { set; get; }
        public string system_prompt { set; get; }
        public string definitions_header { set; get; } = "--- List of translations of definitions ---";
        public List<Definition> definitions { set; get; }
    }
}
