package gov.whitehouse.widget;

public
interface Bindable<T>
{
    public
    void onBindWith(T data, int position);

    public
    void onSetViewClickListener(BaseAdapter.OnItemClickListener listener, int position);
}
