package util;

import java.util.LinkedList;
import java.util.List;

/**
 * @ClassName: Correspondens
 * @description: TODO
 * @author: 小帅丶
 * @create: 2019-05-18
 **/
public class Correspondens {
    private List<Integer> index = new LinkedList<Integer>();

    public List<Integer> getIndex() {
        return index;
    }

    public void setIndex(List<Integer> index) {
        this.index = index;
    }

    public void add(int i) {
        this.index.add(i);
    }
}
