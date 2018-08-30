template = """
    protected static %s LiveData<R> combineLatest(%s) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(%s));

%s
        return s;
    }
"""

def func(n = 3):
    nums = range(1,n+1)
    generic = "<"+ ", ".join([f"T{i}" for i in nums]+["R"])+">"
    params = ", ".join([f"LiveData<T{i}> source{i}" for i in nums]+[f"Function{n}{generic} mapper"])
    ob_params = ", ".join([f"source{i}.getValue()" for i in nums])
    sources = "\n".join([f"        s.addSource(source{i}, observer);" for i in nums])
    return template % (generic, params, ob_params, sources)


for i in range(2, 22):
    print(func(i))
