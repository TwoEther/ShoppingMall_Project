package org.project.shop.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;

    private String author;
    private String publisher;
    private int isbn;
    private LocalDateTime createDate;
    private int pages;
    private String description;

    private boolean saleStatus;

    @ColumnDefault(value = "0")
    private int total_purchase;

    // 이미지 저장을 위한 변수
    private String filePath;
    private String fileName;


    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<CartItem> cartItem = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "categoryItem")
    private Category category;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviewItems = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "items")
    private LikeItem likeItem;

    @OneToMany(mappedBy = "item")
    private List<Inquiry> inquiries = new ArrayList<>();

    public void setLikeItem(LikeItem likeItem) {
        this.likeItem = likeItem;
    }

    public void addTotalPurchase(int volume) {
        if(volume < 0) volume = 0;
        this.total_purchase += volume;
    }

    public void cancelTotalPurchase(int volume) {
        this.total_purchase -= volume;
    }

    public double calculateAvgScore() {
        int total_score =  0;
        for (Review review : this.reviewItems) {
            int score = review.getScore();
            total_score += score;
        }
        // 소수점 첫 째자리 까지
        return Math.round((double) total_score / this.reviewItems.size() * 10) / 10.0;

    }

    @Builder
    public Item(String name, int price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.createDate = LocalDateTime.now();
        this.total_purchase = 0;
        this.saleStatus = true;
    }

    public Item(String name, int price, int stockQuantity, String author, String publisher, int isbn, int pages, String description) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.pages = pages;
        this.description = description;
        this.createDate = LocalDateTime.now();
        this.total_purchase = 0;
        this.saleStatus = true;
    }

    public Item(String name, int price, int stockQuantity, String author, String publisher, int isbn, int pages, String description, String filePath) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.pages = pages;
        this.description = description;
        this.createDate = LocalDateTime.now();
        this.total_purchase = 0;
        this.saleStatus = true;
        this.filePath = filePath;
    }

    public Item() {

    }
    public void setCategory(Category category) {
        if (this.category != null) {
            this.category.getCategoryItem().remove(this);
        }
        this.category = category;
        category.getCategoryItem().add(this);
    }


    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    // 재고 관리를 위한 로직
    public void addStock(int stockQuantity) {
        this.stockQuantity = Math.max(this.stockQuantity + stockQuantity, 0);
    }

    // 재고 관리를 위한 로직
    public void removeStock(int stockQuantity) {
        this.stockQuantity -= stockQuantity;
    }


    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                '}';
    }
}
