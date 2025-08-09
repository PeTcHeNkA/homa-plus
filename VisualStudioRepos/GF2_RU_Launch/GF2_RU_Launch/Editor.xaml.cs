using System.IO;
using System.Text;
using System.Windows;
using SourceChord.FluentWPF;
using System.Text.Json;
using System.Text.Encodings.Web;
using Microsoft.VisualBasic;
using SortJsonUtils;
using System.Windows.Data;
using Microsoft.WindowsAPICodePack.Dialogs;
using System.Text.RegularExpressions;
using Group = SortJsonUtils.Group;
using LangEditAPI;


namespace GF2_RU_Launch
{
    public partial class Editor : AcrylicWindow
    {
        private static byte tableEditorFlag = 0;
        private static byte openDialogFlag = 0;

        private List<ELine> data = new List<ELine>();
        private List<ELine> viewData = new List<ELine>();
        private MainWindow parent;

        private LangEditAPI.Table? table1, table2;
        private ELine? filter;
        private bool hideEmpty = false, hideNonTranslatable = false;
        private bool showId = true;
        private bool locked = false;
        private GroupComparator gcom = new GroupComparator();

        public Editor(MainWindow main)
        {
            parent = main;

            if (!EnsureEditorLoaded())
            {
                Close();
                return;
            }

            InitializeComponent();
            SetExtendViewIntoTitleBar(this, true);
            TTable.ItemsSource = viewData;

            var view = CollectionViewSource.GetDefaultView(TTable.ItemsSource);
        }


        public static bool EnsureEditorLoaded()
        {
            if (tableEditorFlag == 0)
            {
                var i1 = MainWindow.ensureNativeInstalled("langedit.dll");
                if (i1)
                {
                    tableEditorFlag = 1;
                }
                else
                {
                    tableEditorFlag = 2;
                }
            }

            if (tableEditorFlag == 2)
            {
                MessageBox.Show("Модуль редактора таблц не установлен!");
                return false;
            }
            return true;
        }


        public static bool BuildPackNogui(MainWindow? w, string srcPackPath, string srcDir1, string? srcDir2, string outPath)
        {
            var src1 = ScanFileTreeForLangs(new DirectoryInfo(srcPackPath), ".json");
            var src2 = ScanFileTreeForLangs(new DirectoryInfo(srcPackPath), ".json");

            return BuildPackNogui(w, srcPackPath, src1, src2, outPath);
        }


        public static bool BuildPackNogui(MainWindow? w, string srcPackPath, List<string> srcJson1, List<string>? srcJson2, string outPath)
        {
            if (!EnsureEditorLoaded())
            {
                return false;
            }
            Table table = new Table(101);
            try
            {
                table.load(File.ReadAllBytes(srcPackPath));

                LoadJsonTranslations(table, srcJson1);
                if(srcJson2 != null)
                {
                    LoadJsonTranslations(table, srcJson2);
                }
                File.WriteAllBytes(outPath, table.save());
            } catch
            {
                if(w != null)
                {
                    w.onError();
                }
                return false;
            }
            finally
            {
                table.free();
            }
            return true;
        }


        protected override void OnClosed(EventArgs e)
        {
            parent.OnEditorClosed();
        }


        private void setMainData(List<ELine> newData)
        {
            data = newData;
            updateView();
        }

        private void updateView()
        {
            TTable.UnselectAllCells();
            TTable.UnselectAll();
            TTable.ItemsSource = null;

            viewData.Clear();

            var fLtr = filter;

            if (fLtr == null)
            {
                if (hideEmpty || hideNonTranslatable)
                {
                    foreach (ELine cur in data)
                    {
                        if (cur != null)
                        {
                            if (hideEmpty)
                            {
                                if (cur.Original == null || cur.Original.Length == 0)
                                {
                                    if (cur.Value == null || cur.Value.Length == 0)
                                    {
                                        continue;
                                    }
                                }
                            }

                            if (hideNonTranslatable)
                            {
                                if (!isTranslatable(cur.Original)) continue;
                            }

                            viewData.Add(cur);
                        }
                    }
                }
                else
                {
                    viewData.AddRange(data);
                }
            }
            else
            {
                foreach (ELine cur in data)
                {
                    if (cur != null)
                    {
                        if (fLtr.Value != null && cur.Value != null)
                        {
                            if (cur.Value.Contains(fLtr.Value))
                            {
                                viewData.Add(cur);
                            }
                        }
                        else if (fLtr.Original != null && cur.Original != null)
                        {
                            if (hideNonTranslatable)
                            {
                                if (!isTranslatable(cur.Original)) continue;
                            }

                            if (cur.Original.Contains(fLtr.Original))
                            {
                                viewData.Add(cur);
                            }
                        }
                    }
                }
            }

            TTable.ItemsSource = viewData;
        }


        private void onClickLoad(object sender, RoutedEventArgs e)
        {
            if (locked) return;
            locked = true;
            Task.Run(() =>
            {
                var dataBytes = open("Выберите шаблон Lang.bytes", ".bytes", "Lang.bytes|*.bytes");
                if (dataBytes == null)return;
                
                if (table1 != null)table1.free();

                Dispatcher.Invoke(() =>{
                    table1 = new LangEditAPI.Table(1);
                    table1.load(dataBytes);

                    var jData = table1.exportMappings(table2, 0xf);

                    locked = false;
                    onCompleteImportB(jData);
                });
            });
        }


        private void onClickLoadActivePack(object sender, RoutedEventArgs e)
        {
            if (locked) return;
            if (!parent.IsValidGamePath())
            {
                MessageBox.Show("Путь к игре не указан! Автоматический импорт невозможен.");
                return;
            }

            locked = true;
            onClickClose(null, null);

            MainWindow.CallInDedictedThread(() =>
            {
                try
                {
                    onClickLoadActivePackB();
                    var jData = table1.exportMappings(table2, 0xf);

                    Dispatcher.Invoke(() =>
                    {
                        locked = false;
                        onCompleteImportB(jData);
                    });
                }
                catch
                {
                    parent.onError();
                }
                finally
                {
                    locked = false;
                }
            });
        }


        private void onClickLoadActivePackB()
        {
            var cur = parent.getLangPackPath();
            var backup = parent.getBackupPackPath();

            var hasBackup = File.Exists(backup);

            byte[] origBytes = null;
            byte[] curBytes = null;

            if (hasBackup)
            {
                origBytes = File.ReadAllBytes(backup);
                curBytes = File.ReadAllBytes(cur);
            }
            else
            {
                origBytes = File.ReadAllBytes(cur);

                if (File.Exists(MainWindow.GF2_RU_PACK))
                {
                    curBytes = File.ReadAllBytes(MainWindow.GF2_RU_PACK);
                }
            }

            table1 = new LangEditAPI.Table(1);
            table1.load(origBytes);

            if (table2 != null)
            {
                table2.free();
                table2 = null;
            }

            if (curBytes != null)
            {
                table2 = new LangEditAPI.Table(2);
                table2.load(curBytes);
            }
        }


        private void clickKeyFilter(object sender, RoutedEventArgs e)
        {
            var res = Interaction.InputBox("Подстрока", "Введите фильтр", "");
            if (res != null && res.Length > 0)
            {
                if (filter == null)
                {
                    filter = new ELine();
                }

                filter.Original = res;
            }

            updateView();
        }


        private void clickValueFilter(object sender, RoutedEventArgs e)
        {
            var res = Interaction.InputBox("Подстрока", "Введите фильтр", "");
            if (res != null && res.Length > 0)
            {
                if (filter == null)
                {
                    filter = new ELine();
                }

                filter.Value = res;
            }

            updateView();
        }


        private void clickResetFilter(object sender, RoutedEventArgs e)
        {
            hideEmpty = false;
            if (filter != null)
            {
                filter = null;
                updateView();
            }
        }


        private void wanEmptyTable()
        {
            MessageBox.Show("Главная таблица пуста!");
        }


        private void onCompleteImport()
        {
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }

            var jData = table1.exportMappings(table2, 0xf);
            onCompleteImportB(jData);
        }


        private void onCompleteImportB(byte[] jData)
        {
            if (jData != null)
            {
                try
                {
                    var arr = fromJsonBytes(jData);
                    setMainData(arr);
                }
                catch (Exception err)
                {
                    MessageBox.Show(err.Message + "\n" + err.StackTrace);
                    return;
                }
            }

            if (table2 != null)
            {
                table2.free();
                table2 = null;
            }
        }


        private void onClickLoadSecond(object sender, RoutedEventArgs e)
        {
            if (locked) return;
            var dataBytes = open("Выберите релиз Lang.bytes", ".bytes", "Lang.bytes|*.bytes");
            if (dataBytes == null)
            {
                return;
            }

            table2 = new LangEditAPI.Table(2);
            table2.load(dataBytes);

            onCompleteImport();
        }


        private void onClickShowEmpty(object sender, RoutedEventArgs e)
        {
            var prev = hideEmpty;
            hideEmpty = false;
            if (prev != hideEmpty) updateView();
        }


        private void onClickHideEmpty(object sender, RoutedEventArgs e)
        {
            var prev = hideEmpty;
            hideEmpty = true;
            if (prev != hideEmpty) updateView();
        }


        private void onClickHideNonTranslatable(object sender, RoutedEventArgs e)
        {
            var prev = hideNonTranslatable;
            hideNonTranslatable = true;
            if (prev != hideNonTranslatable) updateView();
        }


        private void onClickShowNonTranslatable(object sender, RoutedEventArgs e)
        {
            var prev = hideNonTranslatable;
            hideNonTranslatable = false;
            if (prev != hideNonTranslatable) updateView();
        }


        private void onClickClose(object sender, RoutedEventArgs e)
        {
            if (table1 != null)
            {
                table1.free();
                table1 = null;
            }

            setMainData(new List<ELine>());
        }


        private void onClickLoadLang(object sender, RoutedEventArgs e)
        {
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }

            var result = OpenFiles("Выберите карту переводов mappings.json", ".json", "mappings.json|*.json");
            if (result == null)
            {
                return;
            }
            LoadJsonTranslations(table1, result);

            onCompleteImport();
        }


        private void onClickLoadLandDir(object sender, RoutedEventArgs e)
        {
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }
            string? dir = VisualGetDir("Выберите папку с переводами");
            if (dir != null) 
            { 
                DirectoryInfo info = new DirectoryInfo(dir);
                var paths = ScanFileTreeForLangs(info, ".json");
                LoadJsonTranslations(table1, paths);
                onCompleteImport();
            }
        }


        public static List<string>? OpenFiles(string title, string ext, string filter)
        {
            Microsoft.Win32.OpenFileDialog dlg = new Microsoft.Win32.OpenFileDialog();

            dlg.Title = title;
            dlg.DefaultExt = ext;
            dlg.Filter = filter;
            dlg.Multiselect = true;

            Nullable<bool> result = dlg.ShowDialog();

            if (result == true)
            {
                return new List<string>(dlg.FileNames);
            }
            return null;
        }


        private static List<string> ScanFileTreeForLangs(DirectoryInfo dir, string extension)
        {
            if (!dir.Exists) return Enumerable.Empty<string>().ToList();

            var files = new List<string>();

            foreach (var subDir in dir.EnumerateDirectories())
            {
                files.AddRange(ScanFileTreeForLangs(subDir, extension));
            }

            files.AddRange(dir.EnumerateFiles().Where(inf => inf.Extension == extension)
                .Select(inf => inf.FullName)
            );
            return files;
        }


        private static void LoadJsonTranslations(Table table, List<string> paths)
        {
            paths.AsParallel().Select(str => {
                return File.ReadAllBytes(str);

            }).ForAll(data => {
                table.applyMappngs(data);
            });
        }


        private void onClickExportLangJson(object sender, RoutedEventArgs e)
        {
            exportLang(false);
        }


        private void onClickExportLangJsonID(object sender, RoutedEventArgs e)
        {
            if (!showId)
            {
                MessageBox.Show("В режиме слияния строк экспорт с ID невозможен");
                return;
            }

            exportLang(true);
        }


        private async void onClickExportByGroups(object sender, RoutedEventArgs e)
        {
            var dirName = getAndCheckDir("Выберите папку экспорта");
            if (dirName == null) return;
            await OnExportByGroupsAsync(dirName, data, false);
        }


        private async void onClickExportByGroupsID(object sender, RoutedEventArgs e)
        {
            var dirName = getAndCheckDir("Выберите папку экспорта");
            if (dirName == null) return;
            await OnExportByGroupsAsync(dirName, data, true);
        }


        private string? getAndCheckDir(string lore)
        {
            var dirName = VisualGetDir(lore);
            if (dirName == null) return null;
            try
            {
                var dir = new DirectoryInfo(dirName);
                if (!dir.Exists)
                {
                    dir.Create();
                }
            }
            catch
            {
                MessageBox.Show("Выбран нелегальный путь для сохранения");
                return null;
            }

            return dirName;
        }


        public async Task OnExportByGroupsAsync(string targetDir, List<ELine> src, bool useId)
        {
            var comp = gcom.Copy().MakeCache(src);
            var data = splitByGroups(comp, src);
            var groupedData = data.ToLookup(g => g.Key.Display, g => g.Value)
                .ToDictionary(g => g.Key, g => g.SelectMany(v => v).ToList());
            var tasks = groupedData.AsParallel().Select(group =>
            {
                var resultName = Path.Combine(targetDir, $"{group.Key}.json");

                var directory = Path.GetDirectoryName(resultName);
                Directory.CreateDirectory(directory);
                var bytes = toJsonBytes(group.Value, useId, comp);
                return File.WriteAllBytesAsync(resultName, bytes);
            });
            await Task.WhenAll(tasks);
        }


        public static Dictionary<Group, List<ELine>> splitByGroups(GroupComparator comp, List<ELine> lines)
        {
            Dictionary<Group, List<ELine>> map = new Dictionary<Group, List<ELine>>();
            Group? prevGroup = null;
            List<ELine>? prevEGroup = null;


            List<ELine> nonTrs = new List<ELine>();
            foreach (ELine line in lines)
            {
                if (!isTranslatable(line.Original))
                {
                    nonTrs.Add(line);
                    continue;
                }

                var group = comp.cache.TryGetValue(line.Original, out var cacheGroup)
                    ? cacheGroup
                    : comp.FindGroup(line.Original);
                if (!map.TryGetValue(group, out var groupLines))
                {
                    groupLines = new List<ELine>();
                    map[group] = groupLines;
                }
                groupLines.Add(line);
            }

            if (nonTrs.Count > 0)
            {
                Group nonTranslatable = new Group("", 0, "nontanslatable");
                map[nonTranslatable] = nonTrs;
            }

            return map;
        }


        private void onClickExportLangBytes(object sender, RoutedEventArgs e)
        {
            if (locked) return;
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }

            var res = getSaveFile("Выберите релиз Lang.bytes", ".bytes", "Lang.bytes|*.bytes");

            if (res == null)
            {
                return;
            }
            locked = true;
            MainWindow.CallInDedictedThread(() => {
                var bData = exportTableBytes(showId);
                table1.applyMappngs(bData);

                bData = table1.save();

                try
                {
                    File.WriteAllBytes(res, bData);
                }
                catch (Exception ex)
                {
                    MessageBox.Show("Ошибка сохранения!");
                }
                finally 
                { 
                    locked = false;
                }
            });
        }


        private void onClickEnableMerge(object sender, RoutedEventArgs e)
        {
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }

            var bData = exportTableBytes(showId);
            table1.applyMappngs(bData);

            var jData = table1.exportMappings(null, 2);

            var arr = fromJsonBytes(jData);
            showId = false;
            setMainData(arr);
        }


        private void onClickDisableMerge(object sender, RoutedEventArgs e)
        {
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }

            var bData = exportTableBytes(showId);
            table1.applyMappngs(bData);

            var jData = table1.exportMappings(null, 15);

            var arr = fromJsonBytes(jData);
            showId = true;
            setMainData(arr);
        }


        private void onClicImportSortTable(object sender, RoutedEventArgs e)
        {
            var bytes = open("Выберите правила сортировки rules.txt", ".txt", "rules.txt|*.txt");

            if (bytes == null)
            {
                return;
            }

            try
            {
                var table = UTF8Encoding.UTF8.GetString(bytes);
                gcom = GroupComparator.fromTable(table);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Правила сортировки имеют недопустимый формат!");
            }
        }


        private void onClicResetSortTable(object sender, RoutedEventArgs e)
        {
            gcom = new GroupComparator();
        }

        private void onClicSortByDefRules(object sender, RoutedEventArgs e)
        {
            List<ELine> list2 = viewData;
            list2.Sort(new WrappedLineComparer(new GroupComparator()));
            TTable.ItemsSource = null;
            TTable.ItemsSource = list2;
        }


        private void onClicSortByRules(object sender, RoutedEventArgs e)
        {
            List<ELine> list2 = viewData;

            list2.Sort(new WrappedLineComparer(gcom.Copy().MakeCache(list2)));
            TTable.ItemsSource = null;
            TTable.ItemsSource = list2;
        }

        private class WrappedLineComparer : IComparer<ELine>
        {
            private IComparer<string> parent;

            public WrappedLineComparer(IComparer<string> parent)
            {
                this.parent = parent;
            }

            public int Compare(ELine? x, ELine? y)
            {
                if (x == null)
                {
                    if (y == null) return 0;
                    return 1;
                }
                else
                {
                    if (x == null) return -1;
                }

                return parent.Compare(x.Original, y.Original);
            }
        }

        private byte[] exportTableBytes(bool useId)
        {
            gcom.MakeCache(data);
            return toJsonBytes(data, useId, gcom);
        }

        private void onClickExportNotTranslated(object sender, RoutedEventArgs e)
        {
            exportNonTranslated(false, false);
        }


        private void onClickExportNotTranslatedALL(object sender, RoutedEventArgs e)
        {
            exportNonTranslated(false, true);
        }

        private void onClickExportNotTranslatedID(object sender, RoutedEventArgs e)
        {
            exportNonTranslated(true, false);
        }


        private void onClickExportNotTranslatedIDALL(object sender, RoutedEventArgs e)
        {
            exportNonTranslated(true, false);
        }


        private void exportNonTranslated(bool useId, bool exportNonTranslatable)
        {
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }

            var res = getSaveFile("Выберите карту переводов mappings.json", ".json", "mappings.json|*.json");

            if (res == null)
            {
                return;
            }

            List<ELine> list = new List<ELine>();
            //toJsonBytes(data, useId);
            foreach (ELine item in data)
            {
                if (item != null)
                {
                    if (item.Original != null && item.Original.Length != 0)
                    {
                        if ((item.Value == null || item.Value == "") || item.Value == item.Original)
                        {
                            if (exportNonTranslatable || isTranslatable(item.Original))
                            {
                                list.Add(item);
                            }
                        }
                    }
                }
            }

            gcom.MakeCache(list);
            var data2 = toJsonBytes(list, useId, gcom);

            try
            {
                File.WriteAllBytes(res, data2);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка сохранения!");
            }
        }


        private void exportCurrentView(object sender, RoutedEventArgs e)
        {
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }

            var res = getSaveFile("Выберите карту переводов mappings.json", ".json", "mappings.json|*.json");

            if (res == null)
            {
                return;
            }

            gcom.MakeCache(viewData);
            var data2 = toJsonBytes(viewData, showId, gcom);

            try
            {
                File.WriteAllBytes(res, data2);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка сохранения!");
            }
        }


        private void exportLang(bool useId)
        {
            if (table1 == null)
            {
                wanEmptyTable();
                return;
            }

            var res = getSaveFile("Выберите карту переводов mappings.json", ".json", "mappings.json|*.json");

            if (res == null)
            {
                return;
            }

            var data2 = exportTableBytes(useId);

            try
            {
                File.WriteAllBytes(res, data2);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка сохранения!");
            }
        }


        public static string? VisualGetDir(string title)
        {
            
            string? result = null;
            if (EnsureDirModLoaded())
            {
                result = GetOpenDiU(title);
            }

            return result;
        }


        private static bool EnsureDirModLoaded()
        {
            if (openDialogFlag == 0)
            {
                var i1 = MainWindow.ensureNativeInstalled("Microsoft.WindowsAPICodePack.dll");
                var i2 = MainWindow.ensureNativeInstalled("Microsoft.WindowsAPICodePack.Shell.dll");

                if (i1 && i2)
                {
                    openDialogFlag = 1;
                }
                else
                {
                    openDialogFlag = 2;
                }
            }

            if (openDialogFlag == 2)
            {
                MessageBox.Show("Модуль выбора дериктории отсутствует.");
                return false;
            }
            return true;
        }


        private static string? GetOpenDiU(string title)
        {
            try
            {
                CommonOpenFileDialog dialog = new CommonOpenFileDialog();
                dialog.IsFolderPicker = true;
                if (dialog.ShowDialog() == CommonFileDialogResult.Ok)
                {
                    return dialog.FileName;
                }

                return null;
            }
            catch
            {
                openDialogFlag = 2;
                return null;
            }
        }


        public static string? getSaveFile(string title, string ext, string filter)
        {
            Microsoft.Win32.SaveFileDialog dlg = new Microsoft.Win32.SaveFileDialog();

            dlg.Title = title;
            dlg.DefaultExt = ext;
            dlg.Filter = filter;
            dlg.CheckPathExists = true;

            Nullable<bool> result = dlg.ShowDialog();

            if (result == true)
            {
                return dlg.FileName;
            }

            return null;
        }


        public static byte[]? open(string title, string ext, string filter)
        {
            Microsoft.Win32.OpenFileDialog dlg = new Microsoft.Win32.OpenFileDialog();

            dlg.Title = title;
            dlg.DefaultExt = ext;
            dlg.Filter = filter;

            Nullable<bool> result = dlg.ShowDialog();

            if (result == true)
            {
                return File.ReadAllBytes(dlg.FileName);
            }

            return null;
        }


        public static List<ELine> fromJsonBytes(byte[] data)
        {
            List<ELine> list = new List<ELine>();
            var map = JsonSerializer.Deserialize<Dictionary<string, string>>(UTF8Encoding.UTF8.GetString(data));

            foreach (var item in map)
            {
                var id = getId(item.Key);

                var orig = remId(item.Key);
                ELine eLine = new ELine(id, orig);
                eLine.Value = item.Value;
                list.Add(eLine);
            }

            return list;
        }

        public static byte[] toJsonBytes(List<ELine> list, bool useId, IComparer<string> comparer)
        {
            var map = list.Select(x => { return new { key = x.getSaveKey(useId), value = x.Value }; }).OrderBy(x =>
            {
                if (x.key.StartsWith("#ID:"))
                {
                    int index = x.key.IndexOf("|");
                    if (index == -1) return x.key;
                    return x.key.Substring(index + 1);
                }

                return x.key;
            }, comparer).DistinctBy(x => x.key).ToDictionary(x => x.key, x => x.value);


            var options = new JsonSerializerOptions
            {
                Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
                WriteIndented = true,
            };

            var result = JsonSerializer.Serialize(map, options);
            return UTF8Encoding.UTF8.GetBytes(result);
        }


        public static int getId(string str)
        {
            if (str == null || str.Length <= 5) return 0;
            if (str.StartsWith("#ID:"))
            {
                int index = str.IndexOf('|', 4);
                if (index == -1) return 0;
                int num = 0;
                for (int i = 4; i < index; i++)
                {
                    num *= 10;
                    num += (str[i] - '0');
                }

                return num;
            }

            return 0;
        }


        public static string remId(string src)
        {
            if (src.StartsWith("#ID:"))
            {
                int index = src.IndexOf("|");
                if (index == -1) return src;
                return src[(index + 1)..];
            }

            return src;
        }

        private static Regex trs1 = new Regex("^(?:[^a-zA-Z]*|[^a-zA-Z]*[a-zA-Z][^a-zA-Z]*)$", RegexOptions.Compiled);
        private static Regex trs2 = new Regex("[A-Z][A-Z]-[1-9]-[1-9]", RegexOptions.Compiled);
        
        public static bool isTranslatable(string? str)
        {
            if (String.IsNullOrWhiteSpace(str)) return false;
            if (str.Length == 1)
            {
                return false;
            }
            if (trs2.IsMatch(str))
            {
                return false;
            }
            return !trs1.IsMatch(str);
        }

    }

    public class ELine
    {
        public int Id { get; set; }
        public string? Value { get; set; }
        public string Original { get; set; }

        public ELine()
        {
            Original = "";
        }

        public ELine(int id, string value)
        {
            Id = id;
            Value = value;
            Original = value;
        }


        public string getSaveKey(bool useId)
        {
            if (!useId || Id == 0) return Original;

            return "#ID:" + Id + "|" + Original;
        }

        public string actualValue()
        {
            if (Value != null) return Value;
            return Original;
        }

        public ELine copy()
        {
            ELine copy = new ELine();
            copy.Id = Id;
            copy.Value = Value;
            copy.Original = Original;
            return copy;
        }
    }
}