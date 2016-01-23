uniform sampler2D texture1;

void main() {
    vec4 color = texture2D(texture1, gl_TexCoord[0].st) * gl_Color;
    if (color.b > 1) {
        color = vec4(1);
    }
    gl_FragColor = color;
}