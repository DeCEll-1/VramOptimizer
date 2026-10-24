namespace DDSCreator
{
    public static class Extensions
    {
        public static bool ExecuteIf(this bool condition, Action action)
        {
            if (condition) action();
            return condition;
        }
        public static bool ExecuteIf<T>(this bool condition, Func<T> action)
        {
            if (condition) action();
            return condition;
        }
    }
}
