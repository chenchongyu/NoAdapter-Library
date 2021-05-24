# NoAdapter
一个极简的RecyclerView Adapter 库，可以让开发人员专注于ViewHolder的开发，从而忽略繁冗的adapter的编写和viewholder与model的映射，快速构建复杂的、多类型的列表。

## 快速开始
你是否还在为项目中众多的列表样式而发愁？你是否还在为一遍遍机械重复的编写Adapter代码而厌倦？你是否还在为编写多类型卡片列表的众多细节而一次次的百度？是时候找一个更高效的手段来解脱自己了。NoAdapter——一个为解决Android列表开发的组件库。

正如这个库的名称一样，NoAdapter致力于打造一个极简的RecycleView的Adapter组件，可以让开发者更专注于业务本身的卡片（item）开发，忽略复杂、繁琐、重复（尤其是一个列表有多种卡片类型的情况）的Adapter代码的开发，不用开发一行Adapter代码，真正的做到 No Adapter。

它的优点如下：

1. 极简，只需一行注解即完成开发，具体见下面的使用方法介绍；
2. 对数据结构无依赖；
3. 使用之后可以彻底干掉项目里的XXXAdapter类了。

## 如何使用
### 1.添加依赖和配置
在project的根build.gradle里添加maven仓储
` maven { url 'https://jitpack.io' }`

module下的build.gradle里添加配置和依赖

```
android {
    defaultConfig {
        ...
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [ moduleName : 'xxxx' ]
            }
        }
    }
}

dependencies{
	implementation 'com.github.chenchongyu:NoAdapter-Library:vx.x.x'
	annotationProcessor 'com.github.chenchongyu:NoAdapter-Compiler:vx.x.x'
}
```
同时，添加如下混淆配置：

```
-keep public class com.runningcode.noadapter.adpater.*

-keep public class com.runningcode.noadapter.compiler.*
```

###2.编写ViewHolder
添加`@ViewHolder`注解，继承`BaseVH`类，泛型里指定当前ViewHolder需要的数据类型

```
@ViewHolder
public class UserHolder extends BaseVH<User> {
    private ImageView imageView;

    public UserHolder(ViewGroup parent) {
        super(parent, R.layout.item_image);
        imageView = itemView.findViewById(R.id.image);
    }

    @Override
    public void bindData(User data) {
        imageView.setImageResource(data.image);
    }
}
```
###3.使用
首先要在应用初始化的时候注册一下（建议在application里）

```
ViewHolderRegistry.add(new ViewHolderRegistry_xxx());
ViewHolderRegistry.add(new ViewHolderRegistry_xxxxb());
```
其中ViewHolderRegistry_后面的字符就是你在gradle里配置的moduleName。
当然，这一步也可以省略调，你只需要在project和module的build.gradle里添加如下配置即可：
` classpath 'com.github.chenchongyu:NoAdapter-Plugin:v1.0.0.1'`
`apply plugin: 'no-adapter-plugin'`

```
List allList = new ArrayList();

allList.add(new User());

NoAdapter adapter = new NoAdapter(allList);

mRecyclerView.setAdapter(adapter);

```

如上，一个列表页就开发完了，是不是很简单？

##高阶用法
1.添加ItemClick事件

```
NoAdapter adapter = newNoAdapter(allList);

adapter.setListener(newOnItemClickListener() {

    @Override
    
    publicvoidonItemClick(Object data) {
    
    }
    
});

```

2.如果列表的多类型卡片不是通过数据类型来区分的，而是通过同一个数据类型里的不同字段来区分的？那怎么办？

比如，一个聊天消息列表，根据类型的不同显示文字消息、语音消息、图片消息等，返回的数据结构如下：

```
Class ChatMsg{
    publicString msg;
    publicinttype; // 0:文字；1:语音；2:图片
}

```
在以前的开发过程中，开发者可以自己根据业务在adapter的getItemType()里返回对应卡片的类型，然后根据类型不同来创建不同的卡片。
现在我们已经没有Adapter了，那我们要怎么处理呢 ？
直接上代码：

```
@ViewHolder(cls = ChatMsg.class, filed = "type", type = 0)
public class TextMsgViewHolder extends BaseVH<ChatMsg> {
    private TextView textView;
    private ImageView imageView;
 
    public News1ViewHolder(@NonNull ViewGroup parent) {
        super(parent, R.layout.item_msg_text);
        textView = itemView.findViewById(R.id.text);
        imageView = itemView.findViewById(R.id.image);
    }
 
    @Override
    public void bindData(ChatMsg data) {
        textView.setText(data.title);
        Glide.with(itemView.getContext()).load(data.url).into(imageView);
    }
 
}
 
 
@ViewHolder(cls = ChatMsg.class, filed = "type", type = 1)
public class AudioMsgViewHolder extends BaseVH<ChatMsg> {
    private TextView textView;
    private ImageView imageView;
 
    public News1ViewHolder(@NonNull ViewGroup parent) {
        super(parent, R.layout.item_msg_audio);
        textView = itemView.findViewById(R.id.text);
        imageView = itemView.findViewById(R.id.image);
    }
 
    @Override
    public void bindData(ChatMsg data) {
        textView.setText(data.title);
        Glide.with(itemView.getContext()).load(data.url).into(imageView);
    }
 
}

```
可以看到，还是在viewholder里添加如下注解

`@ViewHolder(cls = ChatMsg.class, filed = "type", type = 1)`

只是在注解里标明对应的数据类型、字段名和字段值即可。

完整demo工程请移步：[NoAdapter-Example](https://github.com/chenchongyu/NoAdapter-Example)



## 讨论
QQ讨论群：984012228
