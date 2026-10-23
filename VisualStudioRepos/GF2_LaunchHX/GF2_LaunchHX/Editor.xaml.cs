using SourceChord.FluentWPF;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace GF2_LaunchHX
{
    public partial class Editor : AcrylicWindow
    {
        private MainWindow mw;
        public Editor(MainWindow mw)
        {
            this.mw = mw;
            SetExtendViewIntoTitleBar(this, true);
            InitializeComponent();
        }


        private void OnClickLoad(object sender, RoutedEventArgs e)
        {

        }


        private void OnClickClose(object sender, RoutedEventArgs e)
        {

        }


        private void OnClickLoadSecond(object sender, RoutedEventArgs e)
        {

        }

        private void OnClickLoadLang(object sender, RoutedEventArgs e)
        {

        }

        private void OnClickLoadLandDir(object sender, RoutedEventArgs e)
        {

        }

        private void OnClickLoadActivePack(object sender, RoutedEventArgs e)
        {

        }


        private void OnClicImportSortTable(object sender, RoutedEventArgs e)
        {

        }


        private void OnClickExportLangBytes(object sender, RoutedEventArgs e)
        {

        }


        private void OnClickExportCurrentView(object sender, RoutedEventArgs e)
        {

        }


        private void OnClickExportLangJson(object sender, RoutedEventArgs e)
        {

        }


        private void OnClickExportLangJsonID(object sender, RoutedEventArgs e)
        {

        }


        private void OnClickExportNotTranslated(object sender, RoutedEventArgs e)
        {

        }

        private void OnClickExportNotTranslatedID(object sender, RoutedEventArgs e)
        {

        }

        private void OnClickExportNotTranslatedALL(object sender, RoutedEventArgs e)
        {

        }

        private void OnClickExportNotTranslatedIDALL(object sender, RoutedEventArgs e)
        {

        }

        private void OnClickExportByGroups(object sender, RoutedEventArgs e)
        {

        }

        private void OnClickKeyFilter(object sender, RoutedEventArgs e)
        {

        }


        private void OnClickValueFilter(object sender, RoutedEventArgs e)
        {

        }


        private void OnClickShowEmpty(object sender, RoutedEventArgs e)
        {

        }


        private void OnClickHideEmpty(object sender, RoutedEventArgs e)
        {

        }

        private void OnClickShowNonTranslatable(object sender, RoutedEventArgs e)
        {

        }

        private void OnClickHideNonTranslatable(object sender, RoutedEventArgs e)
        {

        }


        private void OnClickResetFilter(object sender, RoutedEventArgs e)
        {

        }


        private void OnClicSortByDefRules(object sender, RoutedEventArgs e)
        {

        }


        private void OnClicSortByRules(object sender, RoutedEventArgs e)
        {

        }


        private void OnClicResetSortTable(object sender, RoutedEventArgs e)
        {

        }
    }
}
