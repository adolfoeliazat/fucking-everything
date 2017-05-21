using System;
using System.Text;
using Microsoft.Extensions.Logging;

namespace AlrauneBack {
    public static class StringExtensions {
        public static string Capitalize(this string s) {
            return s.Length == 0 ? s : s[0].ToString().ToUpper() + s.Substring(1);
        }
    }

    public static class StringBuilderExtensions {
        public static StringBuilder Ln(this StringBuilder sb, string value) => sb.AppendLine(value);
    }

    public static class Pile {
        public static ILogger<Program> Log;

        public static string T(string en, string ru) => ru;
    }
}

