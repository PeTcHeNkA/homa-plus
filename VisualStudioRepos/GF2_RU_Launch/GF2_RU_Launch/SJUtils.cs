namespace SortJsonUtils
{
    public class GroupComparator : IComparer<string>
    {
        private Group defaultGroup = new Group("", 0, "default");
        private Group[] groups = new Group[0];
        private Group[][] mappingByFirst = new Group[128][];
        private Group[] noMapped = new Group[0];
        public Dictionary<string, Group> cache = new();

        public static GroupComparator fromTable(string data)
        {
            string[] lines = data.Replace("\r","").Split("\n");
            GroupComparator comparator = new GroupComparator();

            foreach (string line in lines)
            {
                if (line.Length == 0) continue;
                var chr = line[0];
                if (chr == '#') continue;

                int index = line.IndexOf('|', 2);
                if (index == -1) continue;
                int num = 0;
                bool invert = line[2] == '-';
                int i = 2;
                if (invert) i = 3;

                for (; i < index; i++)
                {
                    var chr2 = line[i];
                    num *= 10;
                    num += (chr2 - '0');
                }

                int index2 = line.IndexOf('|', index+1);

                if (index2 == -1) index2 = line.Length;

                string groupName = line.Substring(index+1, index2-index-1);

                string display = "";
                if (index2 != line.Length) 
                {
                    display = line.Substring(index2 + 1,line.Length-index2-1).Trim();
                } else
                {
                    display = "group_" + num;
                }

                if (chr == 's')
                {
                    comparator.Add(groupName, num, display);
                } else if(chr == 'c')
                {
                    comparator.AddB(groupName, num, display);
                }
            }
            comparator.Complete();
            return comparator;
        }

        public void Add(string prefix, int sortIndex, string display)
        {
            groups = Add0(groups, new Group(prefix, sortIndex, display));
        }

        public void AddB(string prefix, int sortIndex, string display)
        {
            groups = Add0(groups, new GroupB(prefix, sortIndex, display));
        }


        private static Group[] Add0(Group[] groups, Group group)
        {
            if (groups == null) return new Group[] { group };
            Array.Resize(ref groups, groups.Length + 1);
            groups[groups.Length - 1] = group;
            return groups;
        }


        public void Complete()
        {
            Array.Sort(groups, (o1, o2) => o2.Prefix.Length.CompareTo(o1.Prefix.Length));
            int minValue = 0;
            
            foreach (var group in groups)
            {
                if(group.SortIndex < minValue) minValue = group.SortIndex;
                if (group.GetType() != typeof(GroupB))
                {
                    int index = Index(group.Prefix);
                    mappingByFirst[index] = Add0(mappingByFirst[index], group);
                }
                else
                {
                    noMapped = Add0(noMapped, group);
                }
            }
            minValue = (-minValue);

            foreach (var group in groups)
            {
                group.SortIndex += minValue;
            }
        }

        private static int Index(string str)
        {
            if (string.IsNullOrEmpty(str)) return 0;
            return str[0] & 127;
        }

        public void SetDefaultGroup(string prefix, int sortIndex, string dsiplay)
        {
            defaultGroup = new Group(prefix, sortIndex, dsiplay);
        }

        public Group FindGroup(string key)
        {
            var byIndex = mappingByFirst[Index(key)];
            if (byIndex != null)
            {
                foreach (var group in byIndex)
                    if (group.IsSomeGroup(key)) return group;
            }
            foreach (var group in noMapped)
            {
                if (group.IsSomeGroup(key)) return group;
            }
            return defaultGroup;
        }

        public int Compare(string o1r, string o2r)
        {
            string o1 = (o1r);
            string o2 = (o2r);
            Group group1 = cache[o1] ?? FindGroup(o1);
            Group group2 = cache[o2] ?? FindGroup(o2);
            if (group1 != group2)
            {
                int result = group1.SortIndex.CompareTo(group2.SortIndex);
                if (result != 0) 
                    return result;
            }
            int res = o1.Length.CompareTo(o2.Length);
            if (res != 0) return res;
            if (o1 == o1r && o2 == o2r)
                return string.Compare(o1, o2);
            else
            {
                int res2 = string.Compare(o1, o2);
                if (res2 == 0)
                {
                    int id1 = GetId(o1r);
                    int id2 = GetId(o2r);
                    return id1.CompareTo(id2);
                }
                return res2;
            }
        }

        private static int GetId(string src)
        {
            if (src.StartsWith("#ID:"))
            {
                int index = src.IndexOf("|");
                if (index == -1) return 0;
                int num = 0;
                for (int i = 4; i < index; i++)
                {
                    num *= 10;
                    num += (src[i] - '0');
                }
                return num;
            }
            return 0;
        }

        private static string RemId(string src)
        {
            if (src.StartsWith("#ID:"))
            {
                int index = src.IndexOf("|");
                if (index == -1) return src;
                return src.Substring(index + 1);
            }
            return src;
        }
    }

    public class Group
    {
        public string Prefix { get; }
        public int SortIndex { get; set; }
        public string Display { get; }

        public Group(string prefix, int sortIndex, string display)
        {
            Prefix = prefix;
            SortIndex = sortIndex;
            Display = display;
        }

        public virtual bool IsSomeGroup(string key)
        {
            return key.StartsWith(Prefix);
        }
    }


    public class GroupB : Group
    {
        public GroupB(string prefix, int sortIndex, string display) : base(prefix, sortIndex, display) { }

        public override bool IsSomeGroup(string key)
        {
            return key.Contains(Prefix);
        }
    }
}
