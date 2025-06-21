package dao;

import java.util.List;
//định nghĩa các thao tác cơ bản
//interface dùng định nghĩa bộ khung cho các phương thức thao tác với CSDL
public interface DAO<T> {
    List<T> getAll(); //trả về DS các đối tượng T từ CSDL
    T getById(int id); //tìm đối tượng T theo id
    boolean add(T t); //thêm đối tượng T vào CSDL
    boolean update(T t); 
    boolean delete(int id);
} 