package vector;

import javax.activation.UnsupportedDataTypeException;
import java.util.Arrays;

public class Vector<T> {
    private static final int DEFAULT_CAPACITY = 8; //默认容量

    private Object[] data; //实际物理存储数组
    private int size = 0; //元素个数

    public Vector(){
        data = new Object[DEFAULT_CAPACITY];
    }
    public Vector(Vector vector){
        this(vector,0,vector.size);
    }
    public Vector(Vector vector, int low, int high){
        this(vector.toArray(),low,high);
    }
    public Vector(Object[] array){
        this(array,0,array.length);
    }

    /**
     * 不包括high，即[low,high)
     * 如果h-l大于0，则正常复制，否则创建空数组
     */
    public Vector(Object[] array, int low, int high){
        int len = high - low;
        if(len > 0){
            data = new Object[len];
            while(low < high)
                data[size++] = array[low++];//浅拷贝数组内容
        } else {
            data = new Object[DEFAULT_CAPACITY];
        }
    }

    /**
     * 扩展策略：当空间占满时才扩展，调用extendArray()对数组扩容
     * 只有空间不够时，才调扩展方法
     * @param minCapacity 需确保的最小容量
     */
    private void ensureCapacity(int minCapacity){
        if(minCapacity - data.length > 0)
            extendArray(minCapacity);
    }

    /**
     * 数组实际扩展方法，保证容量大于或的关于minCapacity
     * 默认容量翻倍，但如果翻倍仍 < minCapacity 的话，则直接扩容到 minCapacity
     * @param minCapacity
     */
    private void extendArray(int minCapacity){
        int newCapacity = data.length * 2;
        if(newCapacity < minCapacity)
            newCapacity = minCapacity;
        data = Arrays.copyOf(data, newCapacity);
    }

    public void add(T obj){
        ensureCapacity(size+1);
        data[size++] = obj;
    }

    public void insert(int index, T obj){
        rangeCheckForAdd(index);
        ensureCapacity(size+1);
        System.arraycopy(data,index,data,index+1,size-index);
        data[index] = obj;
        size++;
    }
    /**
     * 元素获取方法
     * @param index 下标必须小于元素数量，否则将抛出IndexOutOfBoundsException
     * @return
     */
    public T get(int index){
        rangeCheck(index);
        return (T) data[index];
    }

    /**
     * 元素删除方法，删除后 后面的元素整体向前移动补上
     * @param index
     * @return 被删除的元素
     */
    public T remove(int index){
        rangeCheck(index);
        Object removedObj = data[index];
        System.arraycopy(data, index+1, data, index, size-(index+1));
        data[--size] = null;
        return (T) removedObj;
    }

    private void rangeCheck(int index){
        if(index >= size)
            throw new IndexOutOfBoundsException("index must less than size");
    }
    private void rangeCheckForAdd(int index){
        if(index > size || index <0)
            throw new IndexOutOfBoundsException("index must in [0,"+size+"]");
    }

    public int size(){
        return size;
    }

    /**
     * 从前往后遍历查找，细节参考 indexOf(Object,int,int) 方法
     * @see vector.Vector#indexOf(Object,int,int)
     */
    public int indexOf(Object target){
        return indexOf(target, 0, size);
    }

    /**
     * 在[low,high)区域内查找元素
     * @param target 目标对象
     * @param low 下界，包括
     * @param high 上届，不包括
     * @return 目标对象下标，未找到返回-1
     */
    public int indexOf(Object target, int low, int high){
        if(target == null){
            for(; low<high; low++)
                if(data[low] == null)
                    return low;
        }else{
            for(; low<high; low++)
                if(data[low].equals(target))
                    return low;
        }
        return -1;
    }

    public Object[] toArray(){
        return data;
    }

    /**
     * 无序向量唯一化 最差仍是O(n^2)
     * 非排序情况下感觉这就是优化极限了
     * 遍历元素（未标记删除），对每个元素检查后续是否有重复的。
     * 若有重复，则标记删除，并从重复处往后继续检查直到没有重复元素。继续下个元素
     */
    public void deduplicate_NonSorted(){
        int[] removeFlags = new int[size]; //标志位
        for (int i = 0; i < size; i++) {
            if (removeFlags[i] == 1) //加一个判断，虽然整体复杂度增加了n，只要有2个以上重复就是赚
                continue;

            Object current = data[i];
            int start = i + 1;
            int remove = indexOf(current, start, size);
            while (remove != -1){
                removeFlags[remove] = 1; //标志位置1
                remove = indexOf(current, remove + 1, size);
            }
        }

        // 根据删除标志一次性重建
        rebuildVectorFromRemoveFlags(removeFlags);
    }

    public void sort(){


    }

    protected void checkSupportComparable(){
        for (int i = 0; i < size; i++) {
            if (data[i] instanceof Comparable)
                continue;
            throw new RuntimeException("UnsupportedDataTypeException");
        }
    }

    /**
     *
     */
    private void rebuildVectorFromRemoveFlags(int[] removeFlags){
        if (removeFlags.length > data.length) throw new RuntimeException();

        Object[] newData = new Object[removeFlags.length];
        int newDataIndex = 0;
        for (int i = 0; i < removeFlags.length; i++) {
            if (removeFlags[i] != 1)
                newData[newDataIndex++] = data[i];
        }

        initInternal(newData, newDataIndex); //因为前面已经++了，就不+1了
    }

    private void initInternal(Object[] data, int size){
        if (size > data.length)
            throw new RuntimeException("size must be less than data.length");
        this.data = data;
        this.size = size;
    }


    //toString之所以不用Arrays.toString，是因为需要忽略空数组
    //TODO：这里改成迭代器比较好，当然我这没有继承结构，稍微无所谓一点
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append('[');
        for(int i=0; i<size; i++){
            str.append(data[i]).append(", ");
        }
        if(str.length()>1) {
            str.delete(str.length()-2,str.length());
        }
        str.append(']');
        return str.toString();
    }
}
