uniform sampler2D texture1;

void main() {
    vec4 color = texture2D(texture1, gl_TexCoord[0].st);
    float a = 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b; //Weights reflect human eyesight
    gl_FragColor = vec4(vec3(a), color.a);
}